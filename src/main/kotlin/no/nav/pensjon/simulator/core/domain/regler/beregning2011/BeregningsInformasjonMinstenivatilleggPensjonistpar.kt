package no.nav.pensjon.simulator.core.domain.regler.beregning2011

// Copied from pensjon-regler-api v2.0.0 2026-06-04
class BeregningsInformasjonMinstenivatilleggPensjonistpar {
    /**
     * Beregnet pensjon pensjon
     */
    var samletPensjon = 0.0

    /**
     * Minstepensjonsniva sats
     */
    var mpnSatsOrdinaer = 0.0

    /**
     * Garantipensjonpensjonsniva sats
     */
    var garPNSatsOrdinaer = 0.0

    /**
     * saertillegg sats
     */
    var stSatsOrdinaer = 0.0

    /**
     * tt anvendt brukt i alderspensjon etter kapittel 19
     */
    var tt_anv_AP = 0

    /**
     * tt anvendt brukt i alderspensjon etter kapittel 20
     */
    var tt_anv_AP_Kapittel20 = 0

    /**
     * tt anvendt brukt i uforepensjon
     */
    var tt_anv_UP = 0

    /**
     * trygdetid i prorata beregning
     */
    var prorataUP = 0.0

    /**
     * teller for proratabrok
     */
    var prorataUPTeller = 0

    /**
     * nevner for proratabrok
     */
    var prorataUPNevner = 0

    /**
     * personens gjeldende uttaksgrad
     */
    var uttaksgrad = 0

    /**
     * personens gjeldende uforegrad
     */
    var uforegrad = 0

    //--- Extra:
    constructor() : super() {}

    constructor(beregningsInformasjonMinstenivatilleggPensjonistpar: BeregningsInformasjonMinstenivatilleggPensjonistpar) {
        samletPensjon = beregningsInformasjonMinstenivatilleggPensjonistpar.samletPensjon
        mpnSatsOrdinaer = beregningsInformasjonMinstenivatilleggPensjonistpar.mpnSatsOrdinaer
        stSatsOrdinaer = beregningsInformasjonMinstenivatilleggPensjonistpar.stSatsOrdinaer
        garPNSatsOrdinaer = beregningsInformasjonMinstenivatilleggPensjonistpar.garPNSatsOrdinaer
        tt_anv_AP = beregningsInformasjonMinstenivatilleggPensjonistpar.tt_anv_AP
        tt_anv_AP_Kapittel20 = beregningsInformasjonMinstenivatilleggPensjonistpar.tt_anv_AP_Kapittel20
        tt_anv_UP = beregningsInformasjonMinstenivatilleggPensjonistpar.tt_anv_UP
        prorataUP = beregningsInformasjonMinstenivatilleggPensjonistpar.prorataUP
        prorataUPNevner = beregningsInformasjonMinstenivatilleggPensjonistpar.prorataUPNevner
        prorataUPTeller = beregningsInformasjonMinstenivatilleggPensjonistpar.prorataUPTeller
        uttaksgrad = beregningsInformasjonMinstenivatilleggPensjonistpar.uttaksgrad
        uforegrad = beregningsInformasjonMinstenivatilleggPensjonistpar.uforegrad
    }
    // end extra
}