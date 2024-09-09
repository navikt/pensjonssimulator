package no.nav.pensjon.simulator.core.domain.regler.beregning2011

class BeregningsInformasjonMinstenivatilleggPensjonistpar {
    /*
     * Beregnet pensjon pensjon
     */
    var samletPensjon: Double = 0.0
    /*
     * Minstepensjonsniva sats
     */
    var mpnSatsOrdinaer: Double = 0.0
    /*
     * Garantipensjonpensjonsniva sats
     */
    var garPNSatsOrdinaer: Double = 0.0
    /*
     * saertillegg sats
     */
    var stSatsOrdinaer: Double = 0.0
    /*
     * tt anvendt brukt i alderspensjon etter kapittel 19
     */
    var tt_anv_AP: Int = 0
    /*
     * tt anvendt brukt i alderspensjon etter kapittel 20
     */
    var tt_anv_AP_Kapittel20: Int = 0

    /*
     * tt anvendt brukt i uforepensjon
     */
    var tt_anv_UP: Int = 0
    /*
     * trygdetid i prorata beregning
     */
    var prorataUP: Double = 0.0
    /*
     * teller for proratabrok
     */
    var prorataUPTeller: Int = 0
    /*
     * nevner for proratabrok
     */
    var prorataUPNevner: Int = 0
    /*
     * personens gjeldende uttaksgrad
     */
    var uttaksgrad: Int = 0
    /*
     * personens gjeldende uforegrad
     */
    var uforegrad: Int = 0

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

    constructor(
            samletPensjon: Double = 0.0,
            mpnSatsOrdinaer: Double = 0.0,
            garPNSatsOrdinaer: Double = 0.0,
            stSatsOrdinaer: Double = 0.0,
            tt_anv_AP: Int = 0,
            tt_anv_AP_Kapittel20: Int = 0,
            tt_anv_UP: Int = 0,
            prorataUP: Double = 0.0,
            prorataUPTeller: Int = 0,
            prorataUPNevner: Int = 0,
            uttaksgrad: Int = 0,
            uforegrad: Int = 0) {
        this.samletPensjon = samletPensjon
        this.mpnSatsOrdinaer = mpnSatsOrdinaer
        this.garPNSatsOrdinaer = garPNSatsOrdinaer
        this.stSatsOrdinaer = stSatsOrdinaer
        this.tt_anv_AP = tt_anv_AP
        this.tt_anv_AP_Kapittel20 = tt_anv_AP_Kapittel20
        this.tt_anv_UP = tt_anv_UP
        this.prorataUP = prorataUP
        this.prorataUPTeller = prorataUPTeller
        this.prorataUPNevner = prorataUPNevner
        this.uttaksgrad = uttaksgrad
        this.uforegrad = uforegrad
    }
}
