package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

class FasteUtgifterTilleggUT : Ytelseskomponent {

    var nettoAkk: Int = 0
    var nettoRestAr: Int = 0
    var avkortningsbelopPerAr: Int = 0

    constructor() : super(ytelsekomponentType = YtelsekomponentTypeCti("UT_FAST_UTGIFT_T"))

    constructor(fasteUtgifterTilleggUT: FasteUtgifterTilleggUT) : super(fasteUtgifterTilleggUT) {
        nettoAkk = fasteUtgifterTilleggUT.nettoAkk
        nettoRestAr = fasteUtgifterTilleggUT.nettoRestAr
        avkortningsbelopPerAr = fasteUtgifterTilleggUT.avkortningsbelopPerAr
    }

    constructor(
            nettoAkk: Int = 0,
            nettoRestAr: Int = 0,
            avkortningsbelopPerAr: Int = 0,
            /** super */
            brutto: Int = 0,
            netto: Int = 0,
            fradrag: Int = 0,
            bruttoPerAr: Double = 0.0,
            nettoPerAr: Double = 0.0,
            fradragPerAr: Double = 0.0,
            ytelsekomponentType: YtelsekomponentTypeCti = YtelsekomponentTypeCti("UT_FAST_UTGIFT_T"),
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
    ) {
        this.nettoAkk = nettoAkk
        this.nettoRestAr = nettoRestAr
        this.avkortningsbelopPerAr = avkortningsbelopPerAr
    }
}
