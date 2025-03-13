package no.nav.pensjon.simulator.core.afp.privat

import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.SakType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.VedtakResultatEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.domain.regler.to.BeregnAfpPrivatRequest
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.krav.KravlinjeStatus
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.findLatestDateByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isSameDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.lastDayOfMonthUserTurns67
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.yearUserTurnsGivenAge
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.PensjonTidUtil.OPPTJENING_ETTERSLEP_ANTALL_AAR
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.tech.time.Time
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

// no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.BeregnAfpPrivatHelper
@Component
class PrivatAfpBeregner(
    private val context: SimulatorContext,
    private val generelleDataHolder: GenerelleDataHolder,
    private val time: Time
) {

    // PEN: BeregnAfpPrivatHelper.beregnAfpPrivat
    fun beregnPrivatAfp(afpSpec: PrivatAfpSpec): PrivatAfpResult {
        val simuleringSpec: SimuleringSpec = afpSpec.simulering
        val forrigeBeregningResultat: BeregningsResultatAfpPrivat? = afpSpec.forrigePrivatAfpBeregningResult
        val kravhode: Kravhode = afpSpec.kravhode
        val foersteVirkning: LocalDate = afpSpec.virkningFom
        val afpKravhode: Kravhode = periodiserGrunnlag(privatAfpKravhode(kravhode))
        val soekerGrunnlag: Persongrunnlag = afpKravhode.hentPersongrunnlagForSoker()

        val knekkpunktDatoer: SortedSet<LocalDate> =
            findKnekkpunktDatoer(
                foersteUttakDato = simuleringSpec.foersteUttakDato,
                soekerGrunnlag,
                privatAfpFoersteVirkning = foersteVirkning,
                gjelderOmsorg = afpSpec.gjelderOmsorg
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
        val foedselsdato = soekerGrunnlag.fodselsdato!!.toNorwegianLocalDate()
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

        val beregningResultatListe = beregnPrivatAfpForHvertKnekkpunkt(
            forrigeBeregningResultat,
            foersteVirkning,
            afpKravhode,
            afpVedtak,
            satser,
            knekkpunktDatoer,
            afpSpec.sakId
        )

        val gjeldendeBeregningResultat =
            forrigeBeregningResultat?.let {
                BeregningsResultatAfpPrivat(it).apply {
                    virkTom = tidligsteKnekkpunktDato?.minusDays(1)?.toNorwegianDateAtNoon()
                }
            }

        return PrivatAfpResult(beregningResultatListe, gjeldendeBeregningResultat)
    }

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

        for (knekkpunktDato in knekkpunktDatoer) {
            // Set TOM date on the previous beregningsresultat to the day before the current knekkpunkt
            if (afpBeregningResultat != null) {
                afpBeregningResultat.virkTom = knekkpunktDato.minusDays(1).toNorwegianDateAtNoon()
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
            virkFom = knekkpunktDato.toNorwegianDateAtNoon()
            ft = satser.forholdstall
            justeringsbelop = satser.justeringsbeloep
            referansebelop = satser.referansebeloep
            ftKompensasjonstillegg = satser.kompensasjonstilleggForholdstall
            sisteAfpPrivatBeregning = forrigeAfpBeregningResultat
            virkFomAfpPrivatUttak = afpFoersteVirkning?.toNorwegianDateAtNoon()
        }

        return context.beregnPrivatAfp(request, sakId)
    }

    private fun findKnekkpunktDatoer(
        foersteUttakDato: LocalDate?,
        soekerGrunnlag: Persongrunnlag,
        privatAfpFoersteVirkning: LocalDate?,
        gjelderOmsorg: Boolean
    ): SortedSet<LocalDate> {
        // A knekkpunkt should be created for the year the user turns 63 years of age if and only if the user had opptjening the
        // year he turned 61 years, and hence we need to find that opptjening.
        val relevantOpptjeningAar =
            yearUserTurnsGivenAge(soekerGrunnlag.fodselsdato!!, AFP_ALDER - OPPTJENING_ETTERSLEP_ANTALL_AAR)

        val relevantOpptjeningGrunnlag =
            soekerGrunnlag.opptjeningsgrunnlagListe.firstOrNull { it.ar == relevantOpptjeningAar }

        return findPrivatAfpPerioder(
            foersteUttakDato,
            foedselsdato = soekerGrunnlag.fodselsdato?.toNorwegianLocalDate(),
            opptjeningGrunnlag = relevantOpptjeningGrunnlag,
            privatAfpFoersteVirkning,
            gjelderOmsorg
        )
    }

    private fun findPrivatAfpPerioder(
        foersteUttakDato: LocalDate?,
        foedselsdato: LocalDate?,
        opptjeningGrunnlag: Opptjeningsgrunnlag?,
        privatAfpFoersteVirkning: LocalDate?,
        gjelderOmsorg: Boolean
    ): SortedSet<LocalDate> {
        val knekkpunktDatoer: SortedSet<LocalDate> = TreeSet()

        // Første uttaksdato, but only if equal to første virk for privat AFP
        if (isSameDay(foersteUttakDato, privatAfpFoersteVirkning)) {
            knekkpunktDatoer.add(foersteUttakDato!!)
        }

        // Jan 1st the year the user turns 63 years old, but only if bruker has opptjening the year he/she turns 61 years of age
        opptjeningGrunnlag?.let {
            if (gjelderOmsorg || it.pi > 0) {
                knekkpunktDatoer.add(LocalDate.of(yearUserTurnsGivenAge(foedselsdato!!, AFP_ALDER), 1, 1))
            }
        }

        // 1st of month after user turns 67 years old
        //TODO normert?
        foedselsdato?.let { knekkpunktDatoer.add(lastDayOfMonthUserTurns67(it).plusDays(1)) }

        // Returns only unique values that must be in future and not before første virk privat AFP
        return knekkpunktDatoer.tailSet(findLatestDateByDay(time.today(), privatAfpFoersteVirkning))
    }

    private companion object {
        private const val AFP_ALDER = 63 //TODO normert?

        private fun calculateAfpBeholdningDato(spec: SimuleringSpec, foedselsdato: LocalDate): LocalDate {
            val aarSoekerOppnaarAfpAlder: Int = yearUserTurnsGivenAge(foedselsdato, AFP_ALDER)

            return if (spec.foersteUttakDato!!.year < aarSoekerOppnaarAfpAlder)
                LocalDate.of(aarSoekerOppnaarAfpAlder, 1, 1)
            else
                spec.foersteUttakDato
        }

        private fun newKravlinje(person: PenPerson) =
            Kravlinje().apply {
                kravlinjeTypeEnum = KravlinjeTypeEnum.AFP_PRIVAT
                hovedKravlinje = KravlinjeTypeEnum.AFP_PRIVAT.erHovedkravlinje
                relatertPerson = person
                kravlinjeStatus = KravlinjeStatus.VILKARSPROVD
                land = LandkodeEnum.NOR
            }

        /**
         * Creates a kravhode specific for privat AFP, based on existing kravhode.
         * NB: Need to copy persongrunnlag (which will be modified in AFP-specific way).
         */
        private fun privatAfpKravhode(kravhode: Kravhode) =
            Kravhode().apply {
                sakType = SakType.AFP_PRIVAT
                regelverkTypeEnum = kravhode.regelverkTypeEnum
                persongrunnlagListe.add(
                    Persongrunnlag(
                        source = kravhode.hentPersongrunnlagForSoker(),
                        excludeForsteVirkningsdatoGrunnlag = true,
                        excludeTrygdetidPerioder = true
                    ).also {
                        it.finishInit()
                    })
            }

        private fun newInnvilgetVedtak(
            soeker: PenPerson,
            kravlinje: Kravlinje,
            foersteVirkningFom: LocalDate?,
            virkningFom: LocalDate
        ) =
            VilkarsVedtak().apply {
                this.forsteVirk = foersteVirkningFom?.toNorwegianDateAtNoon()
                this.penPerson = soeker
                this.kravlinje = kravlinje
                this.kravlinjeTypeEnum = kravlinje.kravlinjeTypeEnum
                this.vilkarsvedtakResultatEnum = VedtakResultatEnum.INNV
                this.virkFom = virkningFom.toNorwegianDateAtNoon()
            }.also {
                it.finishInit()
            }

        private fun periodiserGrunnlag(kravhode: Kravhode): Kravhode {
            kravhode.persongrunnlagListe.forEach {
                if (it.personDetaljListe.isNotEmpty()) {
                    for (personDetalj in it.personDetaljListe) {
                        if (personDetalj.bruk != true) {
                            it.personDetaljListe.remove(personDetalj)
                        }
                    }
                }
            }

            return kravhode
        }
    }
}
