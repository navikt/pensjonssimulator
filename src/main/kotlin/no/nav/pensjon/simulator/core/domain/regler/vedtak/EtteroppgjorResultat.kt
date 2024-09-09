package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.kode.EtteroppgjorResultatCti

class EtteroppgjorResultat(
    /**
     * Totalbeløp for ytelsene uføretrygd og gjenlevendetillegg basert på tidligere vedtak, som var gjeldende (løpende eller i iverksettelsesløpet) i etteroppgjørsåret ved
     * beregning
     */
    var tidligereBelopUT: Int = 0,

    /**
     * Totalbeløp for ytelsen barnetillegg fellesbarn basert på tidligere vedtak, som var gjeldende (løpende eller i iverksettelsesløpet) i etteroppgjørsåret ved beregning
     */
    var tidligereBelopTFB: Int = 0,

    /**
     * Totalbeløp for ytelsen barnetillegg særkullsbarn basert på tidligere vedtak, som var gjeldende (løpende eller i iverksettelsesløpet) i etteroppgjørsåret ved beregning
     */
    var tidligereBelopTSB: Int = 0,

    /**
     * Totalbeløp for delytelsene uføretrygd, gjenlevendetillegg, barnetillegg særkullsbarn og barnetillegg fellesbarn (sum av tidligereBelopUT, tidligereBelopTFB,
     * tidligereBelopTSB)
     */
    var tidligereBelop: Int = 0,

    /**
     * Total beregnet uføretrygd og gjenlevendetillegg i etteroppgjørsåret
     */
    var totalBelopUT: Int = 0,

    /**
     * Totalt beregnet barnetillegg fellesbarn i etteroppgjørsåret
     */
    var totalBelopTFB: Int = 0,

    /**
     * Totalt beregnet barnetillegg særkullsbarn i etteroppgjørsåret
     */
    var totalBelopTSB: Int = 0,

    /**
     * Totalt beregnet uføretrygd, gjenlevendetillegg, barnetillegg særkullsbarn og barnetillegg fellesbarn i etteroppgjørsåret (sum av totalBelopUT, totalBelopTFB, totalBelopTSB)
     */
    var totalBelop: Int = 0,

    /**
     * Angir kronebeløp for et helt rettsgebyr
     */
    var rettsgebyr: Int = 0,

    /**
     * Resultatet av etteroppgjøret.
     */
    var etteroppgjorResultatType: EtteroppgjorResultatCti? = null,

    /**
     * Toleransegrense for etterbetaling.
     */
    var toleransegrensePositiv: Int = 0,

    /**
     * Toleransegrense for tilbakekreving.
     */
    var toleransegrenseNegativ: Int = 0,

    /**
     * Representerer inntekten som legges til grunn for beregningen av etteroppgjøret for uføretrygden.
     */
    var inntektUT: Int = 0,

    /**
     * Inntekten som legges til grunn for beregningen av etteroppgjøret for barnetillegget for fellesbarn
     */
    var inntektTFB: Int = 0,

    /**
     * Inntekten som legges til grunn for beregningen av etteroppgjøret for barnetillegget for særkullsbarn
     */
    var inntektTSB: Int = 0,

    /**
     * Det totale avviksbeløpet i UT, TFB og TSB.
     */
    var avviksbelop: Int = 0,

    /**
     * Beløpet som utgjør differansen mellom uføretrygd og ev. gjenlevendetillegg basert på forventet og uføretrygd og ev. gjenlevendetillegg basert på lignet inntekt.
     */
    var avviksbelopUT: Int = 0,

    /**
     * Beløpet som utgjør differansen mellom barnetillegget for fellesbarn basert på forventet og barnetillegget for fellesbarn basert på lignet inntekt
     */
    var avviksbelopTFB: Int = 0,

    /**
     * Beløpet som utgjør differansen mellom barnetillegget for særkullsbarn basert på forventet og barnetillegget for særkullsbarn basert på lignet inntekt
     */
    var avviksbelopTSB: Int = 0
) {
    constructor(etteroppgjorResultat: EtteroppgjorResultat) : this() {
        this.tidligereBelop = etteroppgjorResultat.tidligereBelop
        this.tidligereBelopTFB = etteroppgjorResultat.tidligereBelopTFB
        this.tidligereBelopTSB = etteroppgjorResultat.tidligereBelopTSB
        this.tidligereBelopUT = etteroppgjorResultat.tidligereBelopUT
        this.totalBelop = etteroppgjorResultat.totalBelop
        this.totalBelopTFB = etteroppgjorResultat.totalBelopTFB
        this.totalBelopTSB = etteroppgjorResultat.totalBelopTSB
        this.totalBelopUT = etteroppgjorResultat.totalBelopUT
        this.rettsgebyr = etteroppgjorResultat.rettsgebyr
        this.inntektUT = etteroppgjorResultat.inntektUT
        this.inntektTFB = etteroppgjorResultat.inntektTFB
        this.inntektTSB = etteroppgjorResultat.inntektTSB
        this.avviksbelop = etteroppgjorResultat.avviksbelop
        this.avviksbelopUT = etteroppgjorResultat.avviksbelopUT
        this.avviksbelopTFB = etteroppgjorResultat.avviksbelopTFB
        this.avviksbelopTSB = etteroppgjorResultat.avviksbelopTSB
        if (etteroppgjorResultat.etteroppgjorResultatType != null) {
            this.etteroppgjorResultatType = EtteroppgjorResultatCti(etteroppgjorResultat.etteroppgjorResultatType)
        }
        this.toleransegrensePositiv = etteroppgjorResultat.toleransegrensePositiv
        this.toleransegrenseNegativ = etteroppgjorResultat.toleransegrenseNegativ
    }
}
