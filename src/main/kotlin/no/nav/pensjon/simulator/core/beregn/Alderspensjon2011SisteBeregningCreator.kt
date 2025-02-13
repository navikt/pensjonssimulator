package no.nav.pensjon.simulator.core.beregn

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.krav.KravService
import org.springframework.stereotype.Component

@Component
class Alderspensjon2011SisteBeregningCreator(kravService: KravService) : SisteBeregningCreatorBase(kravService) {

    // AbstraktOpprettSisteAldersberegning.execute
    override fun createBeregning(
        spec: SisteBeregningSpec,
        beregningResultat: AbstraktBeregningsResultat?
    ): SisteBeregning =
        newSisteAldersberegning2011(spec).also {
            populate(it, spec, beregningResultat)
        }

    // OpprettSisteAldersberegning2011Regelverk2011.createAndInitSisteBeregning
    private fun newSisteAldersberegning2011(spec: SisteBeregningSpec): SisteBeregning =
        SisteAldersberegning2011().apply {
            regelverkTypeEnum = spec.regelverkKodePaNyttKrav
            alderspensjonBeregningResultat(spec)?.let { populateAldersberegning(source = it, sink = this) }
            setAnvendtGjenlevenderettVedtak(sink = this, vedtakList = spec.filtrertVilkarsvedtakList)
        }

    // OpprettSisteAldersberegning2011Regelverk2011.populateSisteBeregningFromBeregningsresultat2011
    private fun populateAldersberegning(
        source: BeregningsResultatAlderspensjon2011,
        sink: SisteAldersberegning2011
    ) {
        source.virkFom?.let { sink.virkDato = it }
        source.benyttetSivilstandEnum?.let { sink.benyttetSivilstandEnum = it }
        source.pensjonUnderUtbetaling?.let { sink.pensjonUnderUtbetaling = utenIrrelevanteYtelseskomponenter(it) }
        source.beregningKapittel19?.tt_anv?.let { sink.tt_anv = it }
        source.beregningKapittel19?.resultatTypeEnum?.let { sink.resultatTypeEnum = it }
        source.beregningKapittel19?.basispensjon?.let { sink.basispensjon = Basispensjon(it) }
        source.beregningKapittel19?.restpensjon?.let { sink.restpensjon = Basispensjon(it) }
        source.beregningsInformasjonKapittel19?.epsMottarPensjon?.let { sink.epsMottarPensjon = it }
        source.beregningsInformasjonKapittel19?.gjenlevenderettAnvendt?.let { sink.gjenlevenderettAnvendt = it }
    }

    private companion object {
        private fun alderspensjonBeregningResultat(spec: SisteBeregningSpec) =
            spec.beregningsresultat as? BeregningsResultatAlderspensjon2011
    }
}
