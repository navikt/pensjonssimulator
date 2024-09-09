package no.nav.pensjon.simulator.core.domain.regler

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengtall
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.LonnsvekstInformasjon

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

    fun finishInit() {
        rawPoengtall = poengtall
        poengtall = rawPoengtall?.let(::simplePoengtall)
    }

    private fun simplePoengtall(source: Poengtall) =
        Poengtall().apply {
            gv = source.gv
            uforear = source.uforear
        }

    constructor(o: Opptjening) : this() {
        this.ar = o.ar
        this.opptjeningsgrunnlag = o.opptjeningsgrunnlag
        this.anvendtOpptjeningsgrunnlag = o.anvendtOpptjeningsgrunnlag
        this.arligOpptjening = o.arligOpptjening
        if (o.lonnsvekstInformasjon != null) {
            this.lonnsvekstInformasjon = LonnsvekstInformasjon(o.lonnsvekstInformasjon!!)
        }
        if (o.poengtall != null) {
            this.poengtall = Poengtall(o.poengtall!!)
        }
        this.pSatsOpptjening = o.pSatsOpptjening
        this.inntektUtenDagpenger = o.inntektUtenDagpenger
        if (o.uforeOpptjening != null) {
            this.uforeOpptjening = Uforeopptjening(o.uforeOpptjening!!)
        }
        this.dagpenger = o.dagpenger
        this.dagpengerFiskerOgFangstmenn = o.dagpengerFiskerOgFangstmenn
        this.omsorg = o.omsorg
        this.forstegangstjeneste = o.forstegangstjeneste
        this.arligOpptjeningOmsorg = o.arligOpptjeningOmsorg
        this.arligOpptjeningUtenOmsorg = o.arligOpptjeningUtenOmsorg
        this.antFgtMnd = o.antFgtMnd
        this.samletDagpenger = o.samletDagpenger
        this.samletUtbetalteDagpenger = o.samletUtbetalteDagpenger
        this.samletFerietillegg = o.samletFerietillegg
        this.samletBarnetillegg = o.samletBarnetillegg
    }
}
