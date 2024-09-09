package no.nav.pensjon.simulator.core.domain.regler.beregning2011

/**
 * Angir detaljer rund avkortingen av uføretrygd.
 */
class AvkortingsinformasjonUT : AbstraktAvkortingsinformasjon {
    /**
     * Beløpsgrense.
     */
    var belopsgrense: Int = 0

    /**
     * Sum av inntektskomponentene som ble lagt til grunn.
     */
    var forventetInntekt: Int = 0

    /**
     * Inntekt under denne grensen gir ikke utslag i avkorting.
     */
    var inntektsgrense: Int = 0

    /**
     * Inntektsgrense nest år settes når neste års inntektsgrense beregnes
     */
    var inntektsgrenseNesteAr: Int = 0

    /**
     * Inntektstaket for påfølgende år fastsatt på bakgrunn av siste gjeldende OIFU i året. Feltet er kun angitt dersom inntektstak neste år avviker fra gjeldende inntektstak.
     */
    var inntektstakNesteAr: Int = 0
    /**
     * Angir dekningsgrad av tapt arbeidsevne.
     */
    var kompensasjonsgrad: Double = 0.0

    /**
     * Oppjustert inntekt etter uførhet.
     */

    var oieu: Int = 0

    /**
     * Oppjustert inntekt før uførhet.
     */

    var oifu: Int = 0

    /**
     * Den OIFU som er angitt i beregningsperioden. Denne er ikke nødvendigvis den høyeste i året og skal benyttes for beregning av brutto barnetillegg.
     */

    var oifuForBarnetillegg: Int = 0

    /**
     * Beregnet årlig bruttobeløp etter full uføregrad.
     */

    var ugradertBruttoPerAr: Int = 0

    /**
     * Utbetalingsgrad etter inntektsavkorting.
     */

    var utbetalingsgrad: Int = 0

    /**
     * Beløpet som skal legges til avkortningsbeløpet for å få fradraget som er nødvendig for å kompensere for tidligere for lite eller mye avkortet.
     */
    var differansebelop: Int = 0

    constructor() : super()

    constructor(avkortingsinformasjonUT: AvkortingsinformasjonUT) : super(avkortingsinformasjonUT) {
        oifu = avkortingsinformasjonUT.oifu
        oieu = avkortingsinformasjonUT.oieu
        belopsgrense = avkortingsinformasjonUT.belopsgrense
        inntektsgrense = avkortingsinformasjonUT.inntektsgrense
        ugradertBruttoPerAr = avkortingsinformasjonUT.ugradertBruttoPerAr
        kompensasjonsgrad = avkortingsinformasjonUT.kompensasjonsgrad
        utbetalingsgrad = avkortingsinformasjonUT.utbetalingsgrad
        forventetInntekt = avkortingsinformasjonUT.forventetInntekt
        inntektsgrenseNesteAr = avkortingsinformasjonUT.inntektsgrenseNesteAr
        inntektstakNesteAr = avkortingsinformasjonUT.inntektstakNesteAr
        differansebelop = avkortingsinformasjonUT.differansebelop
        oifuForBarnetillegg = avkortingsinformasjonUT.oifuForBarnetillegg
    }

    constructor(
            belopsgrense: Int = 0,
            forventetInntekt: Int = 0,
            inntektsgrense: Int = 0,
            inntektsgrenseNesteAr: Int = 0,
            inntektstakNesteAr: Int = 0,
            kompensasjonsgrad: Double = 0.0,
            oieu: Int = 0,
            oifu: Int = 0,
            oifuForBarnetillegg: Int = 0,
            ugradertBruttoPerAr: Int = 0,
            utbetalingsgrad: Int = 0,
            differansebelop: Int = 0
    ) {
        this.belopsgrense = belopsgrense
        this.forventetInntekt = forventetInntekt
        this.inntektsgrense = inntektsgrense
        this.inntektsgrenseNesteAr = inntektsgrenseNesteAr
        this.inntektstakNesteAr = inntektstakNesteAr
        this.kompensasjonsgrad = kompensasjonsgrad
        this.oieu = oieu
        this.oifu = oifu
        this.oifuForBarnetillegg = oifuForBarnetillegg
        this.ugradertBruttoPerAr = ugradertBruttoPerAr
        this.utbetalingsgrad = utbetalingsgrad
        this.differansebelop = differansebelop
    }
}
