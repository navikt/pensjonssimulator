package no.nav.pensjon.simulator.core.domain.regler

import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengtall
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.LonnsvekstInformasjon
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011.copy
import no.nav.pensjon.simulator.core.domain.reglerextend.copy

// Copied from pensjon-regler-api v2.4.2 2026-09-04
/**
 * Dataholderklasse for opptjening. Objektet vil være både input og output fra regeltjenester.
 */
class Opptjening {
    var ar = 0
    var opptjeningsgrunnlag = 0.0
    var anvendtOpptjeningsgrunnlag = 0.0
    var arligOpptjening = 0.0
    var lonnsvekstInformasjon: LonnsvekstInformasjon? = null
    var pSatsOpptjening = 0.0
    var poengtall: Poengtall? = null
    var inntektUtenDagpenger = 0.0
    var uforeOpptjening: Uforeopptjening? = null
    var dagpenger = 0.0
    var dagpengerFiskerOgFangstmenn = 0.0
    var omsorg = 0.0
    var forstegangstjeneste = 0.0
    var arligOpptjeningOmsorg = 0.0
    var arligOpptjeningUtenOmsorg = 0.0

    //--- Extra:

    /**
     * Ref. PEN: CommonToPen.mapOpptjeningToPen (poengtall part)
     */
    fun finishInit() {
        poengtall = poengtall?.let(::simplePoengtall)
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

    constructor()

    constructor(source: Opptjening) {
        ar = source.ar
        opptjeningsgrunnlag = source.opptjeningsgrunnlag
        anvendtOpptjeningsgrunnlag = source.anvendtOpptjeningsgrunnlag
        arligOpptjening = source.arligOpptjening
        lonnsvekstInformasjon = source.lonnsvekstInformasjon?.copy()
        pSatsOpptjening = source.pSatsOpptjening
        poengtall = source.poengtall?.let(::Poengtall)
        inntektUtenDagpenger = source.inntektUtenDagpenger
        uforeOpptjening = source.uforeOpptjening?.copy()
        dagpenger = source.dagpenger
        dagpengerFiskerOgFangstmenn = source.dagpengerFiskerOgFangstmenn
        omsorg = source.omsorg
        forstegangstjeneste = source.forstegangstjeneste
        arligOpptjeningOmsorg = source.arligOpptjeningOmsorg
        arligOpptjeningUtenOmsorg = source.arligOpptjeningUtenOmsorg
    }
}