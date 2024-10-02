package no.nav.pensjon.simulator.core.beregn

import no.nav.pensjon.simulator.core.domain.Land
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.krav.KravlinjeStatus
import no.nav.pensjon.simulator.core.domain.RegelverkType
import no.nav.pensjon.simulator.core.domain.VedtakResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteBeregning
import no.nav.pensjon.simulator.core.domain.regler.kode.VilkarsvedtakResultatCti
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.util.toLocalDate
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

@Component
class SisteBeregningCreator(
    private val context: SimulatorContext,
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
        beregningResult: AbstraktBeregningsResultat?
    ): SisteBeregning? {
        validate(beregning = null, kravhode = kravhode, virkningFom = null, beregningResult = beregningResult)

        val spec: SisteBeregningSpec =
            createOpprettSisteBeregningParameter(
                beregning = null,
                kravhode = kravhode,
                vedtakListe = vedtakListe,
                virkningFom = null,
                beregningResult = beregningResult
            )

        if (spec.isRegelverk1967 && spec.regelverk1967VirkToEarly) {
            return null
        }

        spec.regelverkKodePaNyttKrav = spec.kravhode?.regelverkTypeCti?.let { RegelverkType.valueOf(it.kode) }

        spec.forrigeKravhode =
            findForrigeKravhode(beregningResult, kravhode.kravId)
                ?: periodiserGrunnlag(spec.kravhode!!, spec.fomDato, spec.tomDato) // periodiserGrunnlagFraNyttKrav

        return sisteBeregningCreator(spec).createBeregning(spec, beregningResult)
    }

    // OpprettSisteBeregningCommand.findForrigeKravhode
    private fun findForrigeKravhode(
        beregningsresultat: AbstraktBeregningsResultat?,
        gjeldendeKravId: Long?
    ): Kravhode? {
        /*ANON
        val kravId = beregningsresultat?.kravId

        if (kravId == gjeldendeKravId) {
            return null
        }

        return kravId?.let {
            periodiserGrunnlag(
                kravhode = context.getKravhode(it),
                fom = beregningsresultat.virkFom,
                tom = beregningsresultat.virkTom
            )
        }
        */
        return null
    }

    // OpprettSisteBeregningCommand.createOpprettSisteBeregningInstance
    private fun sisteBeregningCreator(spec: SisteBeregningSpec): SisteBeregningCreatorBase {
        val regelverkType = spec.regelverkKodePaNyttKrav

        if (spec.isRegelverk1967) {
            throw RuntimeException("Regelverk1967 not supported")
        }

        return when (regelverkType) {
            RegelverkType.N_REG_G_N_OPPTJ -> alderspensjon2016SisteBeregningCreator
            RegelverkType.N_REG_N_OPPTJ -> alderspensjon2025SisteBeregningCreator
            RegelverkType.N_REG_G_OPPTJ -> alderspensjon2011SisteBeregningCreator
            else -> throw RuntimeException("Unexpected regelverkType $regelverkType in SisteBeregningCreator")
        }
    }

    private companion object {
        // OpprettSisteBeregningCommand.REGULERINGS_DATO
        private val reguleringDato = LocalDate.of(2011, 5, 1)

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
            beregning: AbstraktBeregningsResultat?,
            kravhode: Kravhode,
            vedtakListe: List<VilkarsVedtak>,
            virkningFom: LocalDate?,
            beregningResult: AbstraktBeregningsResultat?
        ): SisteBeregningSpec {
            val isRegelverk1967 = beregning != null

            if (isRegelverk1967) {
                val etterRegulering = isAfterByDay(virkningFom, reguleringDato, true)

                return SisteBeregningSpec(
                    isRegelverk1967 = true,
                    kravhode = kravhode,
                    beregning = beregning,
                    beregningsresultat = beregningResult,
                    vilkarsvedtakListe = vedtakListe,
                    regelverk1967VirkToEarly = etterRegulering,
                    fomDato = if (etterRegulering) null else beregning?.virkFom.toLocalDate(),
                    tomDato = if (etterRegulering) null else beregning?.virkTom.toLocalDate(),
                    filtrertVilkarsvedtakList = emptyList(),
                    forrigeKravhode = null,
                    regelverkKodePaNyttKrav = null
                )
            }

            val filtrertVedtakListe: List<VilkarsVedtak> =
                beregningResult?.virkFom?.let { filtrerVedtak(it, beregningResult.virkTom, vedtakListe) }
                //?: throw ImplementationUnrecoverableException("Missing beregningsresultat.virkFom")
                    ?: throw RuntimeException("Missing beregningsresultat.virkFom")

            return SisteBeregningSpec(
                isRegelverk1967 = false,
                kravhode = kravhode,
                beregning = beregning,
                beregningsresultat = beregningResult,
                vilkarsvedtakListe = vedtakListe,
                fomDato = beregningResult.virkFom.toLocalDate(),
                tomDato = beregningResult.virkTom.toLocalDate(),
                filtrertVilkarsvedtakList = filtrertVedtakListe,
                forrigeKravhode = null,
                regelverkKodePaNyttKrav = null
            )
        }

        // SimpleBeregningService.filtrerVilkarsvedtak
        // -> FiltrerVilkarsvedtakCommand.execute
        private fun filtrerVedtak(fom: Date, tom: Date?, vedtakListe: List<VilkarsVedtak>): List<VilkarsVedtak> =
            vedtakListe.filter {
                isInnvilget(it.vilkarsvedtakResultat)
                        && isVilkarsprovdOrFerdig(it.kravlinje)
                        && isNorsk(it.kravlinje)
                        && isVirkFomBeforeDate(it.virkFom, fom)
                        && isVirkTomAfterDate(it.virkTom, tom)
            }

        // FiltrerVilkarsvedtakCommand.isVilkarsvedtakInnvilget
        private fun isInnvilget(vedtak: VilkarsvedtakResultatCti?): Boolean =
            VedtakResultat.INNV.name == vedtak?.kode

        // FiltrerVilkarsvedtakCommand.isKravlinjeStatusVilkarsprovdOrFerdig
        private fun isVilkarsprovdOrFerdig(kravlinje: Kravlinje?): Boolean =
            EnumSet.of(KravlinjeStatus.VILKARSPROVD, KravlinjeStatus.FERDIG).contains(kravlinje?.kravlinjeStatus)

        // FiltrerVilkarsvedtakCommand.isKravlinjeNor
        private fun isNorsk(kravlinje: Kravlinje?): Boolean =
            Land.NOR == kravlinje?.land

        // FiltrerVilkarsvedtakCommand.isVirkFomBeforeFomDate
        private fun isVirkFomBeforeDate(virkFom: Date?, date: Date): Boolean =
            isBeforeByDay(virkFom, date, true)

        // FiltrerVilkarsvedtakCommand.isVirkTomAfterTomDate
        private fun isVirkTomAfterDate(virkTom: Date?, date: Date?): Boolean =
            when {
                virkTom == null && date == null -> true
                virkTom == null -> true
                date == null -> false
                else -> isAfterByDay(virkTom, date, true)
            }

        // OpprettSisteBeregningCommand.validateRequest
        private fun validate(
            beregning: AbstraktBeregningsResultat?,
            kravhode: Kravhode,
            virkningFom: LocalDate?,
            beregningResult: AbstraktBeregningsResultat?
        ) {
            requireNotNull(kravhode) { "kravhode is required" }
            require(beregning != null || beregningResult != null) { "beregning and beregningsresultat cannot both be null" }
            require(beregning == null || beregningResult == null) { "beregning and beregningsresultat cannot both be set" }
            //require(beregningsresultat == null || vilkarsvedtakListe != null) { "vilkarsvedtakListe is required when beregningsresultat is set" }
            require(beregning == null || virkningFom != null) { "datoVirkFom is required when beregning is set" }
        }
    }
}
