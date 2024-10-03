package no.nav.pensjon.simulator.core.beregn

import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.kode.BorMedTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.RegelverkTypeCti
import org.springframework.stereotype.Component

@Component
class Alderspensjon2011SisteBeregningCreator(context: SimulatorContext) : SisteBeregningCreatorBase(context) {

    // AbstraktOpprettSisteAldersberegning.execute
    override fun createBeregning(input: SisteBeregningSpec, beregningsresultat: AbstraktBeregningsResultat?) =
        newSisteAldersberegning2016(input).also {
            populate(it, input, beregningsresultat)
        }

    // OpprettSisteAldersberegning2016Regelverk2016.createAndInitSisteBeregning
    private fun newSisteAldersberegning2016(input: SisteBeregningSpec): SisteBeregning =
        SisteAldersberegning2016().apply {
            regelverkType = input.regelverkKodePaNyttKrav?.let { RegelverkTypeCti(it.name) }
            val resultat2016 = input.beregningsresultat as? BeregningsResultatAlderspensjon2016
            setBeregningsresultat2016Data(this, resultat2016)
            pensjonUnderUtbetaling2011 =
                utenIrrelevanteYtelseskomponenter(resultat2016?.beregningsResultat2011?.pensjonUnderUtbetaling)
            pensjonUnderUtbetaling2025 =
                utenIrrelevanteYtelseskomponenter(resultat2016?.beregningsResultat2025?.pensjonUnderUtbetaling)

            if (resultat2016?.beregningsResultat2011 != null) {
                setAnvendtGjenlevenderettVedtak(this, input.filtrertVilkarsvedtakList)
            }
        }

    // OpprettSisteAldersberegning2016Regelverk2016.populateSisteBeregningFromBeregningsresultat2016
    private fun setBeregningsresultat2016Data(
        sink: SisteAldersberegning2016,
        source: BeregningsResultatAlderspensjon2016?
    ) {
        val resultat2011: BeregningsResultatAlderspensjon2011? = source?.beregningsResultat2011
        val resultat2025: BeregningsResultatAlderspensjon2025? = source?.beregningsResultat2025
        val beregningKapittel19: AldersberegningKapittel19? = resultat2011?.beregningKapittel19
        val beregningKapittel20: AldersberegningKapittel20? = resultat2025?.beregningKapittel20

        if (beregningKapittel20 != null) {
            setAlternativKonvensjonData(sink, beregningKapittel20, beregningKapittel20.beregningsMetode)
            sink.beregningsMetode = beregningKapittel20.beregningsMetode
            sink.prorataBrok_kap_20 = beregningKapittel20.prorataBrok
            sink.tt_anv_kap_20 = beregningKapittel20.tt_anv
            sink.beholdninger = beregningKapittel20.beholdninger
            sink.resultatType = beregningKapittel19?.resultatType ?: beregningKapittel20.resultatType
        }

        sink.virkDato = source?.virkFom
        sink.pensjonUnderUtbetaling2011UtenGJR =
            utenIrrelevanteYtelseskomponenter(resultat2011?.pensjonUnderUtbetalingUtenGJR)
        sink.pensjonUnderUtbetaling = utenIrrelevanteYtelseskomponenter(source?.pensjonUnderUtbetaling)
        setKapittel19Data(sink, beregningKapittel19)
        setBenyttetSivilstand(sink, resultat2011?.benyttetSivilstand ?: resultat2025?.benyttetSivilstand)
        setBeregningsinfo(sink, resultat2011, resultat2025)
    }

    // OpprettSisteAldersberegning2016Regelverk2016.populateSisteberegningFromBenyttetSivilstand
    private fun setBenyttetSivilstand(sink: SisteBeregning, source: BorMedTypeCti?) {
        source?.let { sink.benyttetSivilstand = it }
    }

    // OpprettSisteAldersberegning2016Regelverk2016.populateSisteberegningFromBeregningsInformasjon
    private fun setBeregningsinfo(
        sink: SisteAldersberegning2016,
        source2011: BeregningsResultatAlderspensjon2011?,
        source2025: BeregningsResultatAlderspensjon2025?
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
            restpensjon?.let { sink.restpensjon = Basispensjon(it) }
            basispensjon?.let { sink.basispensjon = Basispensjon(it) }
            restpensjonUtenGJR?.let { sink.restpensjonUtenGJR = Basispensjon(it) }
            basispensjonUtenGJR?.let { sink.basispensjonUtenGJR = Basispensjon(it) }
        }
    }
}
