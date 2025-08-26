package no.nav.pensjon.simulator.afp.privat

import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.VedtakResultatEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.domain.regler.to.BeregnAfpPrivatRequest
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011.copy
import no.nav.pensjon.simulator.core.krav.KravlinjeStatus
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.yearUserTurnsGivenAge
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

/**
 * PEN: no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.BeregnAfpPrivatHelper
 */
@Component
class PrivatAfpBeregner(
    private val context: SimulatorContext,
    private val generelleDataHolder: GenerelleDataHolder,
    private val knekkpunktFinder: PrivatAfpKnekkpunktFinder
) {
    // PEN: BeregnAfpPrivatHelper.beregnAfpPrivat
    fun beregnPrivatAfp(spec: PrivatAfpSpec): PrivatAfpResult {
        val foersteVirkning: LocalDate = spec.virkningFom
        val foersteUttakDato: LocalDate? = spec.foersteUttakDato
        val forrigeBeregningResultat: BeregningsResultatAfpPrivat? = spec.forrigePrivatAfpBeregningResult
        val afpKravhode: Kravhode = periodiserGrunnlag(privatAfpKravhode(spec.kravhode))
        val soekerGrunnlag: Persongrunnlag = afpKravhode.hentPersongrunnlagForSoker()

        val knekkpunktDatoer: SortedSet<LocalDate> =
            knekkpunktFinder.findKnekkpunktDatoer(
                foersteUttakDato,
                soekerGrunnlag,
                privatAfpFoersteVirkning = foersteVirkning,
                gjelderOmsorg = spec.gjelderOmsorg
            )

        if (knekkpunktDatoer.isEmpty()) {
            // Ingen knekkpunkter; beregningsresultat fra gjeldende ytelse benyttes videre;
            // not sure if copying needed here (copying was done in PEN legacy code):
            return privatAfpResult(beregningResultat = forrigeBeregningResultat?.copy())
        }

        val afpKravlinje = newKravlinje(soekerGrunnlag.penPerson!!)
        afpKravhode.kravlinjeListe = mutableListOf(afpKravlinje)
        afpKravhode.afpOrdningEnum = AFPtypeEnum.LONHO // any value will do
        val foedselsdato: LocalDate = soekerGrunnlag.fodselsdato!!.toNorwegianLocalDate()
        val satser: PrivatAfpSatser = generelleDataHolder.getPrivatAfpSatser(foersteVirkning, foedselsdato)
        val afpBeholdningDato = foersteUttakDato?.let { calculateAfpBeholdningDato(it, foedselsdato) }
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
            spec.sakId
        )

        val gjeldendeBeregningResultat = forrigeBeregningResultat?.copy()?.apply {
            virkTom = tidligsteKnekkpunktDato?.minusDays(1)?.toNorwegianDateAtNoon()
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

    companion object {
        const val AFP_ALDER = 63 //TODO normert?

        private fun calculateAfpBeholdningDato(foersteUttakDato: LocalDate, foedselsdato: LocalDate): LocalDate {
            val aarSoekerOppnaarAfpAlder: Int = yearUserTurnsGivenAge(foedselsdato, AFP_ALDER)

            return if (foersteUttakDato.year < aarSoekerOppnaarAfpAlder)
                LocalDate.of(aarSoekerOppnaarAfpAlder, 1, 1)
            else
                foersteUttakDato
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
                sakType = SakTypeEnum.AFP_PRIVAT
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

        private fun privatAfpResult(beregningResultat: BeregningsResultatAfpPrivat?) =
            PrivatAfpResult(
                afpPrivatBeregningsresultatListe = beregningResultat?.let(::listOf).orEmpty().toMutableList(),
                gjeldendeBeregningsresultatAfpPrivat = beregningResultat
            )

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
