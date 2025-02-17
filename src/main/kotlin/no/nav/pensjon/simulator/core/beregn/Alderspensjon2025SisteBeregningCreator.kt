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
    private fun newSisteAldersberegning2011(spec: SisteBeregningSpec) =
        SisteAldersberegning2011().apply {
            regelverkTypeEnum = spec.regelverkKodePaNyttKrav
            alderspensjonBeregningResultat(spec)?.let { populateAldersberegning(source = it, sink = this) }
        }

    // OpprettSisteAldersberegning2011Regelverk2025.populateSisteBeregningFromBeregningsresultat2025
    private fun populateAldersberegning(
        source: BeregningsResultatAlderspensjon2025,
        sink: SisteAldersberegning2011
    ) {
        source.virkFom?.let { sink.virkDato = it }
        source.benyttetSivilstandEnum?.let { sink.benyttetSivilstandEnum = it }
        source.pensjonUnderUtbetaling?.let { sink.pensjonUnderUtbetaling = utenIrrelevanteYtelseskomponenter(it) }

        source.beregningKapittel20?.let {
            setAlternativKonvensjonData(
                sink,
                beregningKapittel20 = it,
                vinnendeMetode = it.beregningsMetodeEnum
            )
            sink.beregningsMetodeEnum = it.beregningsMetodeEnum
            sink.prorataBrok_kap_20 = it.prorataBrok
            sink.tt_anv_kap_20 = it.tt_anv
            sink.resultatTypeEnum = it.resultatTypeEnum
            sink.beholdninger = it.beholdninger
        }

        source.beregningsInformasjonKapittel20?.let {
            sink.epsMottarPensjon = it.epsMottarPensjon
            sink.gjenlevenderettAnvendt = it.gjenlevenderettAnvendt
        }
    }

    private companion object {

        private fun alderspensjonBeregningResultat(spec: SisteBeregningSpec): BeregningsResultatAlderspensjon2025? =
            spec.beregningsresultat as? BeregningsResultatAlderspensjon2025

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
}
