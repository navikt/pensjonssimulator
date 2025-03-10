package no.nav.pensjon.simulator.core.domain.regler.beregning2011

/**
 * Angir detaljer rund avkortingen av uføretrygd.
 */
// 2025-09-03
class AvkortingsinformasjonUT : AbstraktAvkortingsinformasjon() {
    /**
     * Beløpsgrense.
     */
    var belopsgrense = 0

    /**
     * Sum av inntektskomponentene som ble lagt til grunn.
     */
    var forventetInntekt = 0

    /**
     * Inntekt under denne grensen gir ikke utslag i avkorting.
     */
    var inntektsgrense = 0

    /**
     * Inntektsgrense nest år settes når neste års inntektsgrense beregnes
     */
    var inntektsgrenseNesteAr = 0

    /**
     * Inntektstaket for påfølgende år fastsatt på bakgrunn av siste gjeldende OIFU i året. Feltet er kun angitt dersom inntektstak neste år avviker fra gjeldende inntektstak.
     */
    var inntektstakNesteAr = 0

    /**
     * Angir dekningsgrad av tapt arbeidsevne.
     */
    var kompensasjonsgrad = 0.0

    /**
     * Oppjustert inntekt etter uførhet.
     */
    var oieu = 0

    /**
     * Oppjustert inntekt før uførhet.
     */
    var oifu = 0

    /**
     * Den OIFU som er angitt i beregningsperioden. Denne er ikke nødvendigvis den høyeste i året og skal benyttes for beregning av brutto barnetillegg.
     */
    var oifuForBarnetillegg = 0

    /**
     * Beregnet årlig bruttobeløp etter full uføregrad.
     */
    var ugradertBruttoPerAr = 0

    /**
     * Utbetalingsgrad etter inntektsavkorting.
     */
    var utbetalingsgrad = 0

    /**
     * Beløpet som skal legges til avkortningsbeløpet for å få fradraget som er nødvendig for å kompensere for tidligere for lite eller mye avkortet.
     */
    var differansebelop = 0
/*
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
*/
}
