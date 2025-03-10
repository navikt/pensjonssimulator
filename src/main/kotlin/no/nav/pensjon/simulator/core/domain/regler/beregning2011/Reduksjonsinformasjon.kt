package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.enum.BarnetilleggregelverktypeEnum

// 2025-09-03
class Reduksjonsinformasjon {
    /**
     * Antall prosent brutto totalytelse ved 100% Uføregrad utgjør av OIFU. Ytelse inkluderer ordinår
     * uføretrygd, barnetillegg og et eventuelt gjenlevendetillegg.
     */
    var andelYtelseAvOIFU = 0.0

    /**
     * Beskriver hvilket regelverk som er benyttet i beregning av regelverk, se kodeverk K_BT_REGELVERK
     */
    var barnetilleggRegelverkTypeEnum: BarnetilleggregelverktypeEnum? = null

    /**
     * Barnetillegg fellesbarn brutto per år
     */
    var btFBEtterReduksjon = 0

    /**
     * Barnetillegg særkullsbarn per år
     */
    var btSBEtterReduksjon = 0

    /**
     * 95% av oppjustert IFU (tak)
     */
    var gradertOppjustertIFU = 0

    /**
     * Samlet brutto etter reduksjon for barnetillegg særkullsbarn/fellesbarn
     */
    var sumBruttoEtterReduksjonBT = 0

    /**
     * Samlet brutto før reduksjon for barnetillegg særkullsbarn/fellesbarn
     */
    var sumBruttoForReduksjonBT = 0

    /**
     * Sum av uføretrygd, gjenlevendetillegg og barnetillegg
     */
    var sumUTBT = 0

    /**
     * Antall felles- og særkullsbarn
     */
    var totaltAntallBarn = 0

    /**
     * Angir prosentsatsen som brukes til å beregne taket for hvor stor samlet ugradert uføretrygd og brutto barnetillegg brukeren kan ha i forhold til oppjustert IFU før brutto
     * barnetillegg blir redusert
     */
    var prosentsatsOIFUForTak = 0

    constructor() : super() {}

    constructor(source: Reduksjonsinformasjon) : super() {
        this.andelYtelseAvOIFU = source.andelYtelseAvOIFU
        this.barnetilleggRegelverkTypeEnum = source.barnetilleggRegelverkTypeEnum
        this.btFBEtterReduksjon = source.btFBEtterReduksjon
        this.btSBEtterReduksjon = source.btSBEtterReduksjon
        this.gradertOppjustertIFU = source.gradertOppjustertIFU
        this.sumBruttoEtterReduksjonBT = source.sumBruttoEtterReduksjonBT
        this.sumBruttoForReduksjonBT = source.sumBruttoForReduksjonBT
        this.sumUTBT = source.sumUTBT
        this.totaltAntallBarn = source.totaltAntallBarn
        this.prosentsatsOIFUForTak = source.prosentsatsOIFUForTak
    }
}
