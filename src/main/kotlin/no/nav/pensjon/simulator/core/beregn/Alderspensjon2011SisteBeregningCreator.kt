package no.nav.pensjon.simulator.core.beregn

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.enum.BorMedTypeEnum
import no.nav.pensjon.simulator.krav.KravService
import org.springframework.stereotype.Component

@Component
class Alderspensjon2011SisteBeregningCreator(kravService: KravService) : SisteBeregningCreatorBase(kravService) {

    // AbstraktOpprettSisteAldersberegning.execute
    override fun createBeregning(spec: SisteBeregningSpec, beregningResultat: AbstraktBeregningsResultat?) =
        newSisteAldersberegning2016(spec).also {
            populate(it, spec, beregningResultat)
        }

    // OpprettSisteAldersberegning2016Regelverk2016.createAndInitSisteBeregning
    private fun newSisteAldersberegning2016(spec: SisteBeregningSpec): SisteBeregning =
        SisteAldersberegning2016().apply {
            regelverkTypeEnum = spec.regelverkKodePaNyttKrav
            val resultat2016 = spec.beregningsresultat as? BeregningsResultatAlderspensjon2016
            setBeregningsresultat2016Data(this, resultat2016)
            pensjonUnderUtbetaling2011 =
                utenIrrelevanteYtelseskomponenter(resultat2016?.beregningsResultat2011?.pensjonUnderUtbetaling)
            pensjonUnderUtbetaling2025 =
                utenIrrelevanteYtelseskomponenter(resultat2016?.beregningsResultat2025?.pensjonUnderUtbetaling)

            if (resultat2016?.beregningsResultat2011 != null) {
                setAnvendtGjenlevenderettVedtak(this, spec.filtrertVilkarsvedtakList)
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
            setAlternativKonvensjonData(sink, beregningKapittel20, beregningKapittel20.beregningsMetodeEnum)
            sink.beregningsMetodeEnum = beregningKapittel20.beregningsMetodeEnum
            sink.prorataBrok_kap_20 = beregningKapittel20.prorataBrok
            sink.tt_anv_kap_20 = beregningKapittel20.tt_anv
            sink.beholdninger = beregningKapittel20.beholdninger
            sink.resultatTypeEnum = beregningKapittel19?.resultatTypeEnum ?: beregningKapittel20.resultatTypeEnum
        }

        sink.virkDato = source?.virkFom
        sink.pensjonUnderUtbetaling2011UtenGJR =
            utenIrrelevanteYtelseskomponenter(resultat2011?.pensjonUnderUtbetalingUtenGJR)
        sink.pensjonUnderUtbetaling = utenIrrelevanteYtelseskomponenter(source?.pensjonUnderUtbetaling)
        setKapittel19Data(sink, beregningKapittel19)
        setBenyttetSivilstand(sink, resultat2011?.benyttetSivilstandEnum ?: resultat2025?.benyttetSivilstandEnum)
        setBeregningsinfo(sink, resultat2011, resultat2025)
    }

    // OpprettSisteAldersberegning2016Regelverk2016.populateSisteberegningFromBenyttetSivilstand
    private fun setBenyttetSivilstand(sink: SisteBeregning, source: BorMedTypeEnum?) {
        source?.let { sink.benyttetSivilstandEnum = it }
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
