package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

class Paragraf_8_5_1_tillegg : Ytelseskomponent {

    constructor() : super(ytelsekomponentType = YtelsekomponentTypeCti("8_5_1_T"))

    constructor(paragraf_8_5_1_tillegg: Paragraf_8_5_1_tillegg) : super(paragraf_8_5_1_tillegg) {}

    constructor(
        /** super Ytelseskomponent*/
        brutto: Int = 0,
        netto: Int = 0,
        fradrag: Int = 0,
        bruttoPerAr: Double = 0.0,
        nettoPerAr: Double = 0.0,
        fradragPerAr: Double = 0.0,
        ytelsekomponentType: YtelsekomponentTypeCti = YtelsekomponentTypeCti("8_5_1_T"),
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
