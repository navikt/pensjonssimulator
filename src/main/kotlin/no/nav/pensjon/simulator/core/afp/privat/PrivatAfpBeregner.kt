package no.nav.pensjon.simulator.core.afp.privat

import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.domain.Land
import no.nav.pensjon.simulator.core.domain.SakType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.VedtakResultatEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.domain.regler.to.BeregnAfpPrivatRequest
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.krav.KravlinjeStatus
import no.nav.pensjon.simulator.core.legacy.util.CopyUtil
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.findLatestDateByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.fromLocalDate
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByDays
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isSameDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.lastDayOfMonthUserTurns67
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.yearUserTurnsGivenAge
import no.nav.pensjon.simulator.core.util.PensjonTidUtil.OPPTJENING_ETTERSLEP_ANTALL_AAR
import no.nav.pensjon.simulator.core.util.toLocalDate
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import org.springframework.stereotype.Component
import java.io.IOException
import java.time.LocalDate
import java.util.*

// no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.BeregnAfpPrivatHelper
@Component
class PrivatAfpBeregner(
    private val context: SimulatorContext,
    private val generelleDataHolder: GenerelleDataHolder
) {

    //@Throws(PEN222BeregningstjenesteFeiletException::class)
    fun beregnPrivatAfp(afpSpec: PrivatAfpSpec): PrivatAfpResult {
        val simuleringSpec: SimuleringSpec = afpSpec.simulering
        val forrigeBeregningResultat: BeregningsResultatAfpPrivat? = afpSpec.forrigePrivatAfpBeregningResult
        val kravhode: Kravhode = afpSpec.kravhode
        val foersteVirkning: LocalDate = afpSpec.virkningFom
        val afpKravhode: Kravhode = periodiserGrunnlag(newAfpPrivatKravhode(kravhode))
        val soekerGrunnlag: Persongrunnlag = afpKravhode.hentPersongrunnlagForSoker()

        val knekkpunktDatoer: SortedSet<LocalDate> =
            findKnekkpunktDatoer(
                simuleringSpec.foersteUttakDato,
                soekerGrunnlag,
                foersteVirkning,
                afpSpec.gjelderOmsorg
            )

        if (knekkpunktDatoer.isEmpty()) {
            // Ingen knekkpunkter; beregningsresultat fra gjeldende ytelse benyttes videre
            if (forrigeBeregningResultat == null) {
                return PrivatAfpResult(mutableListOf(), null)
            }

            //gjeldendeBeregningsresultatAfpPrivat = context.deepCopyBeregningsresultat(forrigeBeregningsresultatAfpPrivat) // NB copying
            val gjeldendeAfpBeregningResultat = forrigeBeregningResultat // assuming copying not needed
            val afpBeregningResultater = arrayListOf(gjeldendeAfpBeregningResultat)
            return PrivatAfpResult(afpBeregningResultater, gjeldendeAfpBeregningResultat)
        }

        val afpKravlinje = newKravlinje(soekerGrunnlag.penPerson!!)
        afpKravhode.kravlinjeListe = mutableListOf(afpKravlinje)
        afpKravhode.afpOrdningEnum = AFPtypeEnum.LONHO // any value will do
        val foedselsdato = soekerGrunnlag.fodselsdato.toLocalDate()!!
        val satser: PrivatAfpSatser = generelleDataHolder.getPrivatAfpSatser(foersteVirkning, foedselsdato)
        val afpBeholdningDato = calculateAfpBeholdningDato(simuleringSpec, foedselsdato)
        soekerGrunnlag.replaceBeholdninger(context.beregnOpptjening(afpBeholdningDato, soekerGrunnlag))
        val tidligsteKnekkpunktDato = knekkpunktDatoer.first()

        val afpVedtak = newInnvilgetVedtak(
            soeker = soekerGrunnlag.penPerson!!,
            kravlinje = afpKravlinje,
            foersteVirkningFom = foersteVirkning,
            virkningFom = tidligsteKnekkpunktDato
        )

        // Call BEF3704 beregnAfpPrivat for each knekkpunkt
        val afpBeregningsresultater = beregnPrivatAfpForHvertKnekkpunkt(
            forrigeBeregningResultat,
            foersteVirkning,
            afpKravhode,
            afpVedtak,
            satser,
            knekkpunktDatoer,
            afpSpec.sakId
        )
        val gjeldendeAfpBeregningsresultat =
            regdomBeregningsresultatAfpPrivat(forrigeBeregningResultat, tidligsteKnekkpunktDato)
        return PrivatAfpResult(afpBeregningsresultater, gjeldendeAfpBeregningsresultat)
    }

    //@Throws(PEN222BeregningstjenesteFeiletException::class)
    private fun beregnPrivatAfpForHvertKnekkpunkt(
        initialForrigePrivatAfpBeregningResultat: BeregningsResultatAfpPrivat?,
        afpFoersteVirkning: LocalDate?,
        afpKravhode: Kravhode,
        afpVedtak: VilkarsVedtak,
        satser: PrivatAfpSatser,
        knekkpunktDatoer: SortedSet<LocalDate>,
        sakId: Long?
    ): MutableList<BeregningsResultatAfpPrivat> {
        var afpBeregningResultat: BeregningsResultatAfpPrivat? = null
        var forrigeAfpBeregningResultat = initialForrigePrivatAfpBeregningResultat
        val afpBeregningResultatListe: MutableList<BeregningsResultatAfpPrivat> = mutableListOf()

        // Call BEF3704 beregnAfpPrivat for each knekkpunkt
        for (knekkpunktDato in knekkpunktDatoer) {
            // Set TOM date on the previous beregningsresultat to the day before the current knekkpunkt
            if (afpBeregningResultat != null) {
                afpBeregningResultat.virkTom = fromLocalDate(getRelativeDateByDays(knekkpunktDato, -1))
            }

            afpBeregningResultat = beregnPrivatAfp(
                forrigeAfpBeregningResultat,
                afpFoersteVirkning,
                afpKravhode,
                afpVedtak,
                satser,
                knekkpunktDato,
                sakId
            )

            forrigeAfpBeregningResultat = afpBeregningResultat
            afpBeregningResultatListe.add(afpBeregningResultat)
        }

        return afpBeregningResultatListe
    }

    //@Throws(PEN222BeregningstjenesteFeiletException::class)
    private fun beregnPrivatAfp(
        forrigeAfpBeregningResultat: BeregningsResultatAfpPrivat?,
        afpFoersteVirkning: LocalDate?,
        afpKravhode: Kravhode,
        afpVedtak: VilkarsVedtak,
        satser: PrivatAfpSatser,
        knekkpunktDato: LocalDate,
        sakId: Long?
    ): BeregningsResultatAfpPrivat {
        val request = BeregnAfpPrivatRequest().apply {
            kravhode = afpKravhode
            vilkarsvedtakListe = arrayListOf(afpVedtak)
            virkFom = fromLocalDate(knekkpunktDato)
            ft = satser.forholdstall
            justeringsbelop = satser.justeringsbeloep
            referansebelop = satser.referansebeloep
            ftKompensasjonstillegg = satser.kompensasjonstilleggForholdstall
            sisteAfpPrivatBeregning = forrigeAfpBeregningResultat
            virkFomAfpPrivatUttak = fromLocalDate(afpFoersteVirkning)
        }

        return context.beregnPrivatAfp(request, sakId)
    }

    private companion object {
        private const val AFP_ALDER = 63

        private fun findKnekkpunktDatoer(
            foersteUttakDato: LocalDate?,
            soekerGrunnlag: Persongrunnlag,
            privatAfpFoersteVirkning: LocalDate?,
            isOmsorg: Boolean
        ): SortedSet<LocalDate> {
            // A knekkpunkt should be created for the year the user turns 63 years of age if and only if the user had opptjening the
            // year he turned 61 years, and hence we need to find that opptjening.
            val relevantOpptjeningAr =
                yearUserTurnsGivenAge(soekerGrunnlag.fodselsdato!!, AFP_ALDER - OPPTJENING_ETTERSLEP_ANTALL_AAR)

            val relevantOpptjeningsgrunnlag =
                soekerGrunnlag.opptjeningsgrunnlagListe.firstOrNull { it.ar == relevantOpptjeningAr }

            return findPrivatAfpPerioder(
                foersteUttakDato,
                soekerGrunnlag.fodselsdato.toLocalDate(),
                relevantOpptjeningsgrunnlag,
                privatAfpFoersteVirkning,
                isOmsorg
            )
        }

        private fun findPrivatAfpPerioder(
            foersteUttakDato: LocalDate?,
            foedselDato: LocalDate?,
            opptjeningsgrunnlagAtAge61: Opptjeningsgrunnlag?,
            privatAfpFoersteVirkning: LocalDate?,
            isOmsorg: Boolean
        ): SortedSet<LocalDate> {
            val knekkpunktDatoer: SortedSet<LocalDate> = TreeSet()

            // Første uttaksdato, but only if equal to første virk for AFP Privat
            if (isSameDay(foersteUttakDato, privatAfpFoersteVirkning)) {
                knekkpunktDatoer.add(foersteUttakDato!!)
            }

            // Jan 1st the year the user turns 63 years old, but only if bruker has opptjening the year he/she turns 61 years of age
            if (opptjeningsgrunnlagAtAge61 != null) {
                if (isOmsorg || opptjeningsgrunnlagAtAge61.pi > 0) {
                    knekkpunktDatoer.add(LocalDate.of(yearUserTurnsGivenAge(foedselDato!!, AFP_ALDER), 1, 1))
                }
            }

            // 1st of month after user turns 67 years old
            knekkpunktDatoer.add(getRelativeDateByDays(lastDayOfMonthUserTurns67(foedselDato), 1))
            //val today = setTimeToZero(DateProvider.getToday())

            // Returns only unique values that must be in future and not before første virk AFP Privat (CR213963 11.01.2011 OJB2812)
            return knekkpunktDatoer.tailSet(findLatestDateByDay(LocalDate.now(), privatAfpFoersteVirkning))
        }

        private fun calculateAfpBeholdningDato(spec: SimuleringSpec, foedselDato: LocalDate): LocalDate {
            val yearUserTurns63 = yearUserTurnsGivenAge(foedselDato, AFP_ALDER)

            return if (spec.foersteUttakDato!!.year < yearUserTurns63)
                LocalDate.of(yearUserTurns63, 1, 1)
            else
                spec.foersteUttakDato
        }

        private fun newKravlinje(person: PenPerson) =
            Kravlinje().apply {
                kravlinjeTypeEnum = KravlinjeTypeEnum.AFP_PRIVAT
                relatertPerson = person
                kravlinjeStatus = KravlinjeStatus.VILKARSPROVD
                land = Land.NOR
            }

        /**
         * Creates a kravhode specific for AFP privat, based on existing kravhode.
         * NB: Need to copy persongrunnlag (which will be modified in AFP-specific way).
         */
        private fun newAfpPrivatKravhode(kravhode: Kravhode) =
            Kravhode().apply {
                sakType = SakType.AFP_PRIVAT
                regelverkTypeEnum = kravhode.regelverkTypeEnum
                persongrunnlagListe.add(
                    Persongrunnlag(
                        source = kravhode.hentPersongrunnlagForSoker(),
                        excludeForsteVirkningsdatoGrunnlag = true
                    ).also { it.finishInit() })
            }

        private fun newInnvilgetVedtak(
            soeker: PenPerson,
            kravlinje: Kravlinje,
            foersteVirkningFom: LocalDate?,
            virkningFom: LocalDate
        ) =
            VilkarsVedtak().apply {
                this.forsteVirk = fromLocalDate(foersteVirkningFom)
                this.penPerson = soeker
                this.kravlinje = kravlinje
                this.kravlinjeTypeEnum = kravlinje.kravlinjeTypeEnum
                this.vilkarsvedtakResultatEnum = VedtakResultatEnum.INNV
                this.virkFom = fromLocalDate(virkningFom)
            }.also { it.finishInit() }

        private fun periodiserGrunnlag(kravhode: Kravhode): Kravhode {
            for (persongrunnlag in kravhode.persongrunnlagListe) {
                if (persongrunnlag.personDetaljListe.isNotEmpty()) {
                    for (personDetalj in persongrunnlag.personDetaljListe) {
                        if (!personDetalj.bruk) {
                            persongrunnlag.personDetaljListe.remove(personDetalj)
                        }
                    }
                }
            }

            return kravhode
        }

        private fun regdomBeregningsresultatAfpPrivat(
            forrigeBeregningResultat: BeregningsResultatAfpPrivat?,
            earliestKnekkpunktDato: LocalDate?
        ): BeregningsResultatAfpPrivat? {
            if (forrigeBeregningResultat == null) return null

            val gjeldendeAfpBeregningsresultat = try {
                CopyUtil.copy(forrigeBeregningResultat) as BeregningsResultatAfpPrivat
            } catch (e: ClassNotFoundException) {
                //throw ImplementationUnrecoverableException(e)
                throw RuntimeException(e)
            } catch (e: IOException) {
                //throw ImplementationUnrecoverableException(e)
                throw RuntimeException(e)
            }

            gjeldendeAfpBeregningsresultat.virkTom =
                earliestKnekkpunktDato?.let { fromLocalDate(getRelativeDateByDays(it, -1)) }
            return gjeldendeAfpBeregningsresultat
        }
    }
}
