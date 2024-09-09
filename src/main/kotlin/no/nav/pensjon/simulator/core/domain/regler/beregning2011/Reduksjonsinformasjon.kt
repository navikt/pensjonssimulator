package no.nav.pensjon.simulator.core.domain.regler.beregning2011


import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.kode.BarnetilleggRegelverkCti

/**
 * @author Lars Hartvigsen PK-20946
 */
class Reduksjonsinformasjon {
    /**
     * Antall prosent brutto totalytelse ved 100% uføregrad utgjør av OIFU. Ytelse inkluderer ordinær
     * uføretrygd, barnetillegg og et eventuelt gjenlevendetillegg.
     */
    var andelYtelseAvOIFU: Double = 0.0

    /**
     * Beskriver hvilket regelverk som er benyttet i beregning av regelverk, se kodeverk K_BT_REGELVERK
     */
    var barnetilleggRegelverkType: BarnetilleggRegelverkCti? = null

    /**
     * Barnetillegg fellesbarn brutto per år
     */
    var btFBEtterReduksjon: Int = 0

    /**
     * Barnetillegg særkullsbarn per år
     */
    var btSBEtterReduksjon: Int = 0

    /**
     * 95% av oppjustert IFU (tak)
     */
    var gradertOppjustertIFU: Int = 0

    /**
     * Samlet brutto etter reduksjon for barnetillegg særkullsbarn/fellesbarn
     */
    var sumBruttoEtterReduksjonBT: Int = 0

    /**
     * Samlet brutto før reduksjon for barnetillegg særkullsbarn/fellesbarn
     */
    var sumBruttoForReduksjonBT: Int = 0

    /**
     * Sum av uføretrygd, gjenlevendetillegg og barnetillegg
     */
    var sumUTBT: Int = 0

    /**
     * Antall felles- og særkullsbarn
     */
    var totaltAntallBarn: Int = 0

    /**
     * Angir prosentsatsen som brukes til å beregne taket for hvor stor samlet ugradert uføretrygd og brutto barnetillegg brukeren kan ha i forhold til oppjustert IFU før brutto
     * barnetillegg blir redusert
     */
    var prosentsatsOIFUForTak: Int = 0

    var merknader: MutableList<Merknad> = mutableListOf()

    constructor() : super() {}

    constructor(rg: Reduksjonsinformasjon) : super() {
        this.sumUTBT = rg.sumUTBT
        this.gradertOppjustertIFU = rg.gradertOppjustertIFU
        this.totaltAntallBarn = rg.totaltAntallBarn
        this.sumBruttoForReduksjonBT = rg.sumBruttoForReduksjonBT
        this.sumBruttoEtterReduksjonBT = rg.sumBruttoEtterReduksjonBT
        this.btFBEtterReduksjon = rg.btFBEtterReduksjon
        this.btSBEtterReduksjon = rg.btSBEtterReduksjon
        this.prosentsatsOIFUForTak = rg.prosentsatsOIFUForTak
        if (rg.barnetilleggRegelverkType != null) {
            this.barnetilleggRegelverkType = BarnetilleggRegelverkCti(rg.barnetilleggRegelverkType)
        }
        this.merknader = rg.merknader
        this.andelYtelseAvOIFU = rg.andelYtelseAvOIFU
    }

    constructor(
        andelYtelseAvOIFU: Double = 0.0,
        barnetilleggRegelverkType: BarnetilleggRegelverkCti? = null,
        merknader: MutableList<Merknad> = mutableListOf(),
        btFBEtterReduksjon: Int = 0,
        btSBEtterReduksjon: Int = 0,
        gradertOppjustertIFU: Int = 0,
        sumBruttoEtterReduksjonBT: Int = 0,
        sumBruttoForReduksjonBT: Int = 0,
        sumUTBT: Int = 0,
        totaltAntallBarn: Int = 0,
        prosentsatsOIFUForTak: Int = 0
    ) {
        this.andelYtelseAvOIFU = andelYtelseAvOIFU
        this.barnetilleggRegelverkType = barnetilleggRegelverkType
        this.merknader = merknader
        this.btFBEtterReduksjon = btFBEtterReduksjon
        this.btSBEtterReduksjon = btSBEtterReduksjon
        this.gradertOppjustertIFU = gradertOppjustertIFU
        this.sumBruttoEtterReduksjonBT = sumBruttoEtterReduksjonBT
        this.sumBruttoForReduksjonBT = sumBruttoForReduksjonBT
        this.sumUTBT = sumUTBT
        this.totaltAntallBarn = totaltAntallBarn
        this.prosentsatsOIFUForTak = prosentsatsOIFUForTak
    }

}
