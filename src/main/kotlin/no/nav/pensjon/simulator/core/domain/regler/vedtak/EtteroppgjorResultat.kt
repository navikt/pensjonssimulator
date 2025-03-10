package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.enum.EtteroppgjorResultatEnum

// 2025-03-10
class EtteroppgjorResultat {
    /**
     * Totalbeløp for ytelsene uføretrygd og gjenlevendetillegg basert på tidligere vedtak, som var gjeldende (løpende eller i iverksettelsesløpet) i etteroppgjørsåret ved
     * beregning
     */
    var tidligereBelopUT = 0

    /**
     * Totalbeløp for ytelsen barnetillegg fellesbarn basert på tidligere vedtak, som var gjeldende (løpende eller i iverksettelsesløpet) i etteroppgjørsåret ved beregning
     */
    var tidligereBelopTFB = 0

    /**
     * Totalbeløp for ytelsen barnetillegg særkullsbarn basert på tidligere vedtak, som var gjeldende (løpende eller i iverksettelsesløpet) i etteroppgjørsåret ved beregning
     */
    var tidligereBelopTSB = 0

    /**
     * Totalbeløp for delytelsene uføretrygd, gjenlevendetillegg, barnetillegg særkullsbarn og barnetillegg fellesbarn (sum av tidligereBelopUT, tidligereBelopTFB,
     * tidligereBelopTSB)
     */
    var tidligereBelop = 0

    /**
     * Total beregnet uføretrygd og gjenlevendetillegg i etteroppgjørsåret
     */
    var totalBelopUT = 0

    /**
     * Totalt beregnet barnetillegg fellesbarn i etteroppgjørsåret
     */
    var totalBelopTFB = 0

    /**
     * Totalt beregnet barnetillegg særkullsbarn i etteroppgjørsåret
     */
    var totalBelopTSB = 0

    /**
     * Totalt beregnet uføretrygd, gjenlevendetillegg, barnetillegg særkullsbarn og barnetillegg fellesbarn i etteroppgjørsåret (sum av totalBelopUT, totalBelopTFB, totalBelopTSB)
     */
    var totalBelop = 0

    /**
     * Angir kronebeløp for et helt rettsgebyr
     */
    var rettsgebyr = 0

    /**
     * Resultatet av etteroppgjøret.
     */
    var etteroppgjorResultatTypeEnum: EtteroppgjorResultatEnum? = null

    /**
     * Toleransegrense for etterbetaling.
     */
    var toleransegrensePositiv = 0

    /**
     * Toleransegrense for tilbakekreving.
     */
    var toleransegrenseNegativ = 0

    /**
     * Representerer inntekten som legges til grunn for beregningen av etteroppgjøret for uføretrygden.
     */
    var inntektUT = 0

    /**
     * Inntekten som legges til grunn for beregningen av etteroppgjøret for barnetillegget for fellesbarn
     */
    var inntektTFB = 0

    /**
     * Inntekten som legges til grunn for beregningen av etteroppgjøret for barnetillegget for særkullsbarn
     */
    var inntektTSB = 0

    /**
     * Det totale avviksbeløpet i UT, TFB og TSB.
     */
    var avviksbelop = 0

    /**
     * beløpet som utgjør differansen mellom uføretrygd og ev. gjenlevendetillegg basert på forventet og uføretrygd og ev. gjenlevendetillegg basert på lignet inntekt.
     */
    var avviksbelopUT = 0

    /**
     * beløpet som utgjør differansen mellom barnetillegget for fellesbarn basert på forventet og barnetillegget for fellesbarn basert på lignet inntekt
     */
    var avviksbelopTFB = 0

    /**
     * beløpet som utgjør differansen mellom barnetillegget for særkullsbarn basert på forventet og barnetillegget for særkullsbarn basert på lignet inntekt
     */
    var avviksbelopTSB = 0

    constructor()

    constructor(source: EtteroppgjorResultat) : this() {
        this.tidligereBelopUT = source.tidligereBelopUT
        this.tidligereBelopTFB = source.tidligereBelopTFB
        this.tidligereBelopTSB = source.tidligereBelopTSB
        this.tidligereBelop = source.tidligereBelop
        this.totalBelopUT = source.totalBelopUT
        this.totalBelopTFB = source.totalBelopTFB
        this.totalBelopTSB = source.totalBelopTSB
        this.totalBelop = source.totalBelop
        this.rettsgebyr = source.rettsgebyr
        this.etteroppgjorResultatTypeEnum = source.etteroppgjorResultatTypeEnum
        this.toleransegrensePositiv = source.toleransegrensePositiv
        this.toleransegrenseNegativ = source.toleransegrenseNegativ
        this.inntektUT = source.inntektUT
        this.inntektTFB = source.inntektTFB
        this.inntektTSB = source.inntektTSB
        this.avviksbelop = source.avviksbelop
        this.avviksbelopUT = source.avviksbelopUT
        this.avviksbelopTFB = source.avviksbelopTFB
        this.avviksbelopTSB = source.avviksbelopTSB
    }
}
