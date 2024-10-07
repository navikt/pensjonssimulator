package no.nav.pensjon.simulator.core.beregn

import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2025
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteAldersberegning2011
import org.springframework.stereotype.Component

// Corresponds to AbstraktOpprettSisteAldersberegning + OpprettSisteAldersberegning2011Regelverk2025
@Component
class Alderspensjon2025SisteBeregningCreator(context: SimulatorContext) : SisteBeregningCreatorBase(context) {

    // AbstraktOpprettSisteAldersberegning.execute
    override fun createBeregning(spec: SisteBeregningSpec, beregningsresultat: AbstraktBeregningsResultat?) =
        newSisteAldersberegning2011(spec).also {
            populate(it, spec, beregningsresultat)
        }

    // OpprettSisteAldersberegning2011Regelverk2025.createAndInitSisteBeregning
    private fun newSisteAldersberegning2011(spec: SisteBeregningSpec): SisteAldersberegning2011 {
        val sisteBeregning = SisteAldersberegning2011().apply {
            regelverkTypeEnum = spec.regelverkKodePaNyttKrav
        }

        populateSisteBeregningFromBeregningsresultat2025(
            sisteBeregning,
            spec.beregningsresultat as? BeregningsResultatAlderspensjon2025
        )

        return sisteBeregning
    }

    // OpprettSisteAldersberegning2011Regelverk2025.populateSisteBeregningFromBeregningsresultat2025
    private fun populateSisteBeregningFromBeregningsresultat2025(
        beregning: SisteAldersberegning2011,
        beregningsresultat: BeregningsResultatAlderspensjon2025?
    ) {
        val aldersberegningKapittel20 = beregningsresultat?.beregningKapittel20

        aldersberegningKapittel20?.let {
            setAlternativKonvensjonData(beregning, aldersberegningKapittel20, it.beregningsMetodeEnum!!)
            beregning.beregningsMetodeEnum = it.beregningsMetodeEnum
            beregning.prorataBrok_kap_20 = it.prorataBrok
            beregning.tt_anv_kap_20 = it.tt_anv
            beregning.resultatTypeEnum = it.resultatTypeEnum
            beregning.beholdninger = it.beholdninger
        }

        beregning.virkDato = beregningsresultat?.virkFom
        beregning.pensjonUnderUtbetaling = utenIrrelevanteYtelseskomponenter(beregningsresultat?.pensjonUnderUtbetaling)
        beregningsresultat?.benyttetSivilstandEnum?.let { beregning.benyttetSivilstandEnum = it }
        val beregningsinformasjon = beregningsresultat?.beregningsInformasjonKapittel20

        beregningsinformasjon?.let {
            beregning.epsMottarPensjon = it.epsMottarPensjon
            beregning.gjenlevenderettAnvendt = it.gjenlevenderettAnvendt
        }
    }

    /* Assumed to be irrelevant when entities not used
    private fun fixUpPeriodisertPersongrunnlag(persongrunnlag: Persongrunnlag) {
        // To avoid optimistic locking error.
        persongrunnlag.trygdetidList.forEach(::fixUpPeriodisertTrygdetid)
        persongrunnlag.trygdetidKap20List.forEach(::fixUpPeriodisertTrygdetid)
        persongrunnlag.trygdetidAlternativ?.let { it.trygdetidId = null }

        // The periodisert krav itself will not be persisted. To avoid a transient object error, the krav reference is cleared.
        persongrunnlag.kravHode = null
    }

    private fun fixUpPeriodisertTrygdetid(trygdetid: Trygdetid?) {
        if (trygdetid == null) return

        // trygdetidId is copied by FPEN003 periodiserGrunnlag. Must be cleared.
        trygdetid.trygdetidId = null

        // Clear id values of all TtUtlandTrygdeavtale objects. Also copied by FPEN003 periodiserGrunnlag.
        trygdetid.ttUtlandTrygdeavtaler?.let { avtaler -> avtaler.forEach { it.ttUtlandTrygdeavtaleId = null } }
    }
    */

    //TODO:
    /* This method fetches krav from database; assumed to be irrelevant during simulation
    private fun fetchPersongrunnlagFraForrigeKrav(
        beregningsresultat: BeregningHoveddel?,
        kravService: KravServiceBi,
        fpenService: FpenServiceBi): Persongrunnlag? {
        if (beregningsresultat == null || beregningsresultat.kravId == null) {
            return null
        }

        val sisteKravhode: Kravhode = kravService.hentKrav(beregningsresultat.kravId) // NB DB access

        val pgRequest = PeriodiserGrunnlagRequest().apply {
            kravHode = sisteKravhode
            virkDatoFom = beregningsresultat.virkFom
            virkDatoTom = beregningsresultat.virkTom
        }

        val sisteKravhodePeriodisert: Kravhode = fpenService.periodiserGrunnlag(pgRequest).getKravhode()
        return getEpsPersongrunnlag(sisteKravhodePeriodisert)
    }
    */
}
