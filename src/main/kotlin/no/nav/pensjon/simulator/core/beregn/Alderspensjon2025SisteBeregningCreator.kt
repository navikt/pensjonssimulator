package no.nav.pensjon.simulator.core.beregn

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2025
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteAldersberegning2011
import no.nav.pensjon.simulator.krav.KravService
import org.springframework.stereotype.Component

// Corresponds to AbstraktOpprettSisteAldersberegning + OpprettSisteAldersberegning2011Regelverk2025
@Component
class Alderspensjon2025SisteBeregningCreator(kravService: KravService) : SisteBeregningCreatorBase(kravService) {

    // AbstraktOpprettSisteAldersberegning.execute
    override fun createBeregning(spec: SisteBeregningSpec, beregningResultat: AbstraktBeregningsResultat?) =
        newSisteAldersberegning2011(spec).also {
            populate(it, spec, beregningResultat)
        }

    // OpprettSisteAldersberegning2011Regelverk2025.createAndInitSisteBeregning
    private fun newSisteAldersberegning2011(spec: SisteBeregningSpec): SisteAldersberegning2011 {
        val sisteBeregning = SisteAldersberegning2011().apply {
            regelverkTypeEnum = spec.regelverkKodePaNyttKrav
        }

        populateSisteBeregningFromBeregningsresultat2025(
            beregning = sisteBeregning,
            beregningResultat = spec.beregningsresultat as? BeregningsResultatAlderspensjon2025
        )

        return sisteBeregning
    }

    // OpprettSisteAldersberegning2011Regelverk2025.populateSisteBeregningFromBeregningsresultat2025
    private fun populateSisteBeregningFromBeregningsresultat2025(
        beregning: SisteAldersberegning2011,
        beregningResultat: BeregningsResultatAlderspensjon2025?
    ) {
        val aldersberegningKapittel20 = beregningResultat?.beregningKapittel20

        aldersberegningKapittel20?.let {
            setAlternativKonvensjonData(beregning, aldersberegningKapittel20, it.beregningsMetodeEnum!!)
            beregning.beregningsMetodeEnum = it.beregningsMetodeEnum
            beregning.prorataBrok_kap_20 = it.prorataBrok
            beregning.tt_anv_kap_20 = it.tt_anv
            beregning.resultatTypeEnum = it.resultatTypeEnum
            beregning.beholdninger = it.beholdninger
        }

        beregning.virkDato = beregningResultat?.virkFom
        beregning.pensjonUnderUtbetaling = utenIrrelevanteYtelseskomponenter(beregningResultat?.pensjonUnderUtbetaling)
        beregningResultat?.benyttetSivilstandEnum?.let { beregning.benyttetSivilstandEnum = it }
        val beregningsinformasjon = beregningResultat?.beregningsInformasjonKapittel20

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
}
