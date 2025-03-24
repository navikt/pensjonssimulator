package no.nav.pensjon.simulator.core.beregn

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.enum.BorMedTypeEnum
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011.copy
import no.nav.pensjon.simulator.krav.KravService
import org.springframework.stereotype.Component

@Component
class Alderspensjon2016SisteBeregningCreator(kravService: KravService) : SisteBeregningCreatorBase(kravService) {

    // AbstraktOpprettSisteAldersberegning.execute
    override fun createBeregning(spec: SisteBeregningSpec, beregningResultat: AbstraktBeregningsResultat?) =
        newSisteAldersberegning2016(spec).also {
            populate(it, spec, beregningResultat)
        }

    // OpprettSisteAldersberegning2016Regelverk2016.createAndInitSisteBeregning
    private fun newSisteAldersberegning2016(spec: SisteBeregningSpec): SisteBeregning =
        SisteAldersberegning2016().apply {
            regelverkTypeEnum = spec.regelverkKodePaNyttKrav

            alderspensjonBeregningResultat(spec)?.let {
                populateAldersberegning(source = it, sink = this)

                it.beregningsResultat2011?.pensjonUnderUtbetaling?.let {
                    pensjonUnderUtbetaling2011 = utenIrrelevanteYtelseskomponenter(it)
                }

                it.beregningsResultat2025?.pensjonUnderUtbetaling?.let {
                    pensjonUnderUtbetaling2025 = utenIrrelevanteYtelseskomponenter(it)
                }

                if (it.beregningsResultat2011 != null) {
                    setAnvendtGjenlevenderettVedtak(this, spec.filtrertVilkarsvedtakList)
                }
            }
        }

    // OpprettSisteAldersberegning2016Regelverk2016.populateSisteBeregningFromBeregningsresultat2016
    private fun populateAldersberegning(
        source: BeregningsResultatAlderspensjon2016,
        sink: SisteAldersberegning2016
    ) {
        val resultat2011: BeregningsResultatAlderspensjon2011? = source.beregningsResultat2011
        val resultat2025: BeregningsResultatAlderspensjon2025? = source.beregningsResultat2025
        val beregningKapittel19: AldersberegningKapittel19? = resultat2011?.beregningKapittel19

        resultat2025?.beregningKapittel20?.let {
            setAlternativKonvensjonData(sink, beregningKapittel20 = it, vinnendeMetode = it.beregningsMetodeEnum)
            sink.beregningsMetodeEnum = it.beregningsMetodeEnum
            sink.prorataBrok_kap_20 = it.prorataBrok
            sink.tt_anv_kap_20 = it.tt_anv
            sink.beholdninger = it.beholdninger
            sink.resultatTypeEnum = beregningKapittel19?.resultatTypeEnum ?: it.resultatTypeEnum
        }

        sink.virkDato = source.virkFom

        resultat2011?.pensjonUnderUtbetalingUtenGJR?.let {
            sink.pensjonUnderUtbetaling2011UtenGJR = utenIrrelevanteYtelseskomponenter(it)
        }

        source.pensjonUnderUtbetaling?.let { sink.pensjonUnderUtbetaling = utenIrrelevanteYtelseskomponenter(it) }
        setKapittel19Data(sink, beregningKapittel19)
        setBenyttetSivilstand(sink, resultat2011?.benyttetSivilstandEnum ?: resultat2025?.benyttetSivilstandEnum)
        populateAldersberegning(source2011 = resultat2011, source2025 = resultat2025, sink)
    }

    private companion object {

        // OpprettSisteAldersberegning2016Regelverk2016.populateSisteberegningFromBenyttetSivilstand
        private fun setBenyttetSivilstand(sink: SisteBeregning, source: BorMedTypeEnum?) {
            source?.let { sink.benyttetSivilstandEnum = it }
        }

        // OpprettSisteAldersberegning2016Regelverk2016.populateSisteberegningFromBeregningsInformasjon
        private fun populateAldersberegning(
            source2011: BeregningsResultatAlderspensjon2011?,
            source2025: BeregningsResultatAlderspensjon2025?,
            sink: SisteAldersberegning2016
        ) {
            val infoKapittel19 = source2011?.beregningsInformasjonKapittel19
            val infoKapittel20 = source2025?.beregningsInformasjonKapittel20

            (infoKapittel19?.epsMottarPensjon ?: infoKapittel20?.epsMottarPensjon)?.let {
                sink.epsMottarPensjon = it
            }

            (infoKapittel19?.gjenlevenderettAnvendt ?: infoKapittel20?.gjenlevenderettAnvendt)?.let {
                sink.gjenlevenderettAnvendt = it
            }
        }

        // OpprettSisteAldersberegning2016Regelverk2016.populateSisteberegningFromBeregningKapittel19
        private fun setKapittel19Data(sink: SisteAldersberegning2016, source: AldersberegningKapittel19?) {
            if (source == null) return

            with(source) {
                sink.tt_anv = tt_anv
                restpensjon?.let { sink.restpensjon = it.copy() }
                basispensjon?.let { sink.basispensjon = it.copy() }
                restpensjonUtenGJR?.let { sink.restpensjonUtenGJR = it.copy() }
                basispensjonUtenGJR?.let { sink.basispensjonUtenGJR = it.copy() }
            }
        }

        private fun alderspensjonBeregningResultat(spec: SisteBeregningSpec): BeregningsResultatAlderspensjon2016? =
            spec.beregningsresultat as? BeregningsResultatAlderspensjon2016
    }
}
