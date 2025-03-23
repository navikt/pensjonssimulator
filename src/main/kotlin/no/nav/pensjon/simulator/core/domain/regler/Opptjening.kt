package no.nav.pensjon.simulator.core.domain.regler

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengtall
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.LonnsvekstInformasjon
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011.copy

/**
 * Dataholderklasse for opptjening. Objektet vil være både input og output fra regeltjenester.
 */
class Opptjening(
    var ar: Int = 0,
    var opptjeningsgrunnlag: Double = 0.0,
    var anvendtOpptjeningsgrunnlag: Double = 0.0,
    var arligOpptjening: Double = 0.0,
    var lonnsvekstInformasjon: LonnsvekstInformasjon? = null,
    var pSatsOpptjening: Double = 0.0,
    var poengtall: Poengtall? = null,
    var inntektUtenDagpenger: Double = 0.0,
    var uforeOpptjening: Uforeopptjening? = null,
    var dagpenger: Double = 0.0,
    var dagpengerFiskerOgFangstmenn: Double = 0.0,
    var omsorg: Double = 0.0,
    var forstegangstjeneste: Double = 0.0,
    var arligOpptjeningOmsorg: Double = 0.0,
    var arligOpptjeningUtenOmsorg: Double = 0.0,

    @JsonIgnore var antFgtMnd: Double = 0.0,
    @JsonIgnore var samletDagpenger: Double = 0.0,
    @JsonIgnore var samletUtbetalteDagpenger: Double = 0.0,
    @JsonIgnore var samletFerietillegg: Double = 0.0,
    @JsonIgnore var samletBarnetillegg: Double = 0.0
) {
    // SIMDOM-ADD
    @JsonIgnore var rawPoengtall: Poengtall? = null

    /**
     * Ref. PEN: CommonToPen.mapOpptjeningToPen (poengtall part)
     */
    fun finishInit() {
        rawPoengtall = poengtall
        poengtall = rawPoengtall?.let(::simplePoengtall)
    }

    /**
     * Ref. PEN: CommonToPen.mapOpptjeningToPen (poengtall part)
     * There only these poengtall values are mapped:
     * - veietGrunnbelop = Opptjening.poengtall.gv
     * - uforeOpptjening.uforear = Opptjening.poengtall.uforear
     * Therefore only the 'gv' and 'uforear' values from regler are kept.
     */
    private fun simplePoengtall(source: Poengtall) =
        Poengtall().apply {
            gv = source.gv
            uforear = source.uforear
        }

    constructor(source: Opptjening) : this() {
        ar = source.ar
        opptjeningsgrunnlag = source.opptjeningsgrunnlag
        anvendtOpptjeningsgrunnlag = source.anvendtOpptjeningsgrunnlag
        arligOpptjening = source.arligOpptjening
        lonnsvekstInformasjon = source.lonnsvekstInformasjon?.copy()
        poengtall = source.poengtall?.let(::Poengtall)
        rawPoengtall = source.rawPoengtall?.let(::Poengtall)
        pSatsOpptjening = source.pSatsOpptjening
        inntektUtenDagpenger = source.inntektUtenDagpenger
        uforeOpptjening = source.uforeOpptjening?.let(::Uforeopptjening)
        dagpenger = source.dagpenger
        dagpengerFiskerOgFangstmenn = source.dagpengerFiskerOgFangstmenn
        omsorg = source.omsorg
        forstegangstjeneste = source.forstegangstjeneste
        arligOpptjeningOmsorg = source.arligOpptjeningOmsorg
        arligOpptjeningUtenOmsorg = source.arligOpptjeningUtenOmsorg
        antFgtMnd = source.antFgtMnd
        samletDagpenger = source.samletDagpenger
        samletUtbetalteDagpenger = source.samletUtbetalteDagpenger
        samletFerietillegg = source.samletFerietillegg
        samletBarnetillegg = source.samletBarnetillegg
    }
}
