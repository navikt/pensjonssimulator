package no.nav.pensjon.simulator.core.beregn

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteBeregning
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.VedtakResultatEnum
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.krav.KravlinjeStatus
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.krav.KravService
import no.nav.pensjon.simulator.validity.BadSpecException
import no.nav.pensjon.simulator.validity.InternDataInkonsistensException
import no.nav.pensjon.simulator.vedtak.VilkaarsvedtakKravlinje
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

@Component
class SisteBeregningCreator(
    private val kravService: KravService,
    private val alderspensjon2011SisteBeregningCreator: Alderspensjon2011SisteBeregningCreator,
    private val alderspensjon2016SisteBeregningCreator: Alderspensjon2016SisteBeregningCreator,
    private val alderspensjon2025SisteBeregningCreator: Alderspensjon2025SisteBeregningCreator
) {
    // SimuleringEtter2011Context.opprettSisteBeregning
    // -> SimpleFpenService.opprettSisteBeregning
    // -> OpprettSisteBeregningCommand.execute
    fun opprettSisteBeregning(
        kravhode: Kravhode,
        vedtakListe: List<VilkarsVedtak>,
        beregningsresultat: AbstraktBeregningsResultat
    ): SisteBeregning? {
        val spec: SisteBeregningSpec =
            createOpprettSisteBeregningParameter(kravhode, vedtakListe, beregningsresultat)

        if (spec.isRegelverk1967 && spec.regelverk1967VirkToEarly) {
            return null
        }

        spec.regelverkKodePaNyttKrav = spec.kravhode?.regelverkTypeEnum

        spec.forrigeKravhode =
            findForrigeKravhode(beregningsresultat, kravhode.kravId)
                ?: periodiserGrunnlag(spec.kravhode!!, spec.fomDato, spec.tomDato) // periodiserGrunnlagFraNyttKrav

        return sisteBeregningCreator(spec).createBeregning(spec, beregningsresultat)
    }

    // OpprettSisteBeregningCommand.findForrigeKravhode
    private fun findForrigeKravhode(
        beregningsresultat: AbstraktBeregningsResultat?,
        gjeldendeKravId: Long?
    ): Kravhode? {
        val kravId = beregningsresultat?.kravId

        if (kravId == gjeldendeKravId) {
            return null
        }

        return kravId?.let {
            periodiserGrunnlag(
                kravhode = kravService.fetchKravhode(it),
                fom = beregningsresultat.virkFomLd,
                tom = beregningsresultat.virkTomLd
            )
        }
    }

    // OpprettSisteBeregningCommand.createOpprettSisteBeregningInstance
    private fun sisteBeregningCreator(spec: SisteBeregningSpec): SisteBeregningCreatorBase {
        val regelverkType = spec.regelverkKodePaNyttKrav

        if (spec.isRegelverk1967) {
            throw BadSpecException("Regelverk1967 not supported")
        }

        return when (regelverkType) {
            RegelverkTypeEnum.N_REG_G_N_OPPTJ -> alderspensjon2016SisteBeregningCreator
            RegelverkTypeEnum.N_REG_N_OPPTJ -> alderspensjon2025SisteBeregningCreator
            RegelverkTypeEnum.N_REG_G_OPPTJ -> alderspensjon2011SisteBeregningCreator
            else -> throw RuntimeException("Unexpected regelverkType $regelverkType in SisteBeregningCreator")
        }
    }

    private companion object {

        // SimpleFpenService.periodiserGrunnlag
        private fun periodiserGrunnlag(kravhode: Kravhode, fom: LocalDate?, tom: LocalDate?): Kravhode =
            BehandlingPeriodeUtil.periodiserGrunnlag(
                virkningFom = fom,
                virkningTom = tom,
                originalKravhode = kravhode,
                periodiserFomTomDatoUtenUnntak = false, // NB: null in legacy
                sakType = null
            )

        // OpprettSisteBeregningCommand.createOpprettSisteBeregningParameter
        private fun createOpprettSisteBeregningParameter(
            kravhode: Kravhode,
            vedtakListe: List<VilkarsVedtak>,
            beregningsresultat: AbstraktBeregningsResultat?
        ): SisteBeregningSpec {
            val filtrertVedtakListe: List<VilkarsVedtak> =
                beregningsresultat?.virkFomLd?.let {
                    filtrerVedtak(fom = it, tom = beregningsresultat.virkTomLd, vedtakListe)
                } ?: throw InternDataInkonsistensException("Missing beregningsresultat.virkFom")

            return SisteBeregningSpec(
                isRegelverk1967 = false,
                kravhode = kravhode,
                beregning = null,
                beregningsresultat = beregningsresultat,
                vilkarsvedtakListe = vedtakListe,
                fomDato = beregningsresultat.virkFomLd,
                tomDato = beregningsresultat.virkTomLd,
                filtrertVilkarsvedtakList = filtrertVedtakListe,
                forrigeKravhode = null,
                regelverkKodePaNyttKrav = null
            )
        }

        // SimpleBeregningService.filtrerVilkarsvedtak
        // -> FiltrerVilkarsvedtakCommand.execute
        private fun filtrerVedtak(
            fom: LocalDate,
            tom: LocalDate?,
            vedtakListe: List<VilkarsVedtak>
        ): List<VilkarsVedtak> =
            vedtakListe.filter {
                isInnvilget(it.vilkarsvedtakResultatEnum)
                        && isVilkarsprovdOrFerdig(it.kravlinje)
                        && isNorsk(it.kravlinje)
                        && isVirkFomBeforeDate(it.virkFomLd, fom)
                        && isVirkTomAfterDate(it.virkTomLd, tom)
            }

        // FiltrerVilkarsvedtakCommand.isVilkarsvedtakInnvilget
        private fun isInnvilget(vedtak: VedtakResultatEnum?): Boolean =
            VedtakResultatEnum.INNV == vedtak

        // FiltrerVilkarsvedtakCommand.isKravlinjeStatusVilkarsprovdOrFerdig
        private fun isVilkarsprovdOrFerdig(kravlinje: VilkaarsvedtakKravlinje?): Boolean =
            EnumSet.of(KravlinjeStatus.VILKARSPROVD, KravlinjeStatus.FERDIG).contains(kravlinje?.status)

        // FiltrerVilkarsvedtakCommand.isKravlinjeNor
        private fun isNorsk(kravlinje: VilkaarsvedtakKravlinje?): Boolean =
            LandkodeEnum.NOR == kravlinje?.land

        // FiltrerVilkarsvedtakCommand.isVirkFomBeforeFomDate
        private fun isVirkFomBeforeDate(virkFom: LocalDate?, date: LocalDate): Boolean =
            isBeforeByDay(virkFom, date, true)

        // FiltrerVilkarsvedtakCommand.isVirkTomAfterTomDate
        private fun isVirkTomAfterDate(virkTom: LocalDate?, date: LocalDate?): Boolean =
            when {
                virkTom == null && date == null -> true
                virkTom == null -> true
                date == null -> false
                else -> isAfterByDay(virkTom, date, true)
            }
    }
}