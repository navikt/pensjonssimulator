package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

/**
 * Tillegget for faste utgifter. Brukes ved institusjonsopphold.
 */
class FasteUtgifterTillegg : Ytelseskomponent {

    constructor() : super(ytelsekomponentType = YtelsekomponentTypeCti("FAST_UTGIFT_T"))

    constructor(fasteUtgifter: Int) : super(ytelsekomponentType = YtelsekomponentTypeCti("FAST_UTGIFT_T")) {
        netto = fasteUtgifter
    }

    constructor(fasteUtgifterTillegg: FasteUtgifterTillegg) : super(fasteUtgifterTillegg) {}

    constructor(
            /** super Ytelseskomponent*/
            brutto: Int = 0,
            netto: Int = 0,
            fradrag: Int = 0,
            bruttoPerAr: Double = 0.0,
            nettoPerAr: Double = 0.0,
            fradragPerAr: Double = 0.0,
            ytelsekomponentType: YtelsekomponentTypeCti = YtelsekomponentTypeCti("FAST_UTGIFT_T"),
            merknadListe: MutableList<Merknad> = mutableListOf(),
            fradragsTransaksjon: Boolean = false,
            opphort: Boolean = false,
            sakType: SakTypeCti? = null,
            formelKode: FormelKodeCti? = null,
            reguleringsInformasjon: ReguleringsInformasjon? = null
    ) : super(
            brutto = brutto,
            netto = netto,
            fradrag = fradrag,
            bruttoPerAr = bruttoPerAr,
            nettoPerAr = nettoPerAr,
            fradragPerAr = fradragPerAr,
            ytelsekomponentType = ytelsekomponentType,
            merknadListe = merknadListe,
            fradragsTransaksjon = fradragsTransaksjon,
            opphort = opphort,
            sakType = sakType,
            formelKode = formelKode,
            reguleringsInformasjon = reguleringsInformasjon
    )
}
