package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

class Skjermingstillegg : Ytelseskomponent {
    var ft67Soker: Double = 0.0
    var skjermingsgrad: Double = 0.0
    var ufg: Int = 0
    var basGp_bruttoPerAr: Double = 0.0
    var basTp_bruttoPerAr: Double = 0.0
    var basPenT_bruttoPerAr: Double = 0.0

    constructor() : super(ytelsekomponentType = YtelsekomponentTypeCti("SKJERMT"))

    constructor(skjermingstillegg: Skjermingstillegg) : super(skjermingstillegg) {
        ft67Soker = skjermingstillegg.ft67Soker
        skjermingsgrad = skjermingstillegg.skjermingsgrad
        ufg = skjermingstillegg.ufg
        basGp_bruttoPerAr = skjermingstillegg.basGp_bruttoPerAr
        basTp_bruttoPerAr = skjermingstillegg.basTp_bruttoPerAr
        basPenT_bruttoPerAr = skjermingstillegg.basPenT_bruttoPerAr
    }

    constructor(
            ft67Soker: Double,
            skjermingsgrad: Double,
            ufg: Int,
            basGp_bruttoPerAr: Double,
            basTp_bruttoPerAr: Double,
            basPenT_bruttoPerAr: Double
    ) : super(ytelsekomponentType = YtelsekomponentTypeCti("SKJERMT")) {
        this.ft67Soker = ft67Soker
        this.skjermingsgrad = skjermingsgrad
        this.ufg = ufg
        this.basGp_bruttoPerAr = basGp_bruttoPerAr
        this.basTp_bruttoPerAr = basTp_bruttoPerAr
        this.basPenT_bruttoPerAr = basPenT_bruttoPerAr
    }

    constructor(
            ft67Soker: Double = 0.0,
            skjermingsgrad: Double = 0.0,
            ufg: Int = 0,
            basGp_bruttoPerAr: Double = 0.0,
            basTp_bruttoPerAr: Double = 0.0,
            basPenT_bruttoPerAr: Double = 0.0,
            /** super Ytelseskomponent*/
            brutto: Int = 0,
            netto: Int = 0,
            fradrag: Int = 0,
            bruttoPerAr: Double = 0.0,
            nettoPerAr: Double = 0.0,
            fradragPerAr: Double = 0.0,
            ytelsekomponentType: YtelsekomponentTypeCti = YtelsekomponentTypeCti("SKJERMT"),
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
        this.ft67Soker = ft67Soker
        this.skjermingsgrad = skjermingsgrad
        this.ufg = ufg
        this.basGp_bruttoPerAr = basGp_bruttoPerAr
        this.basTp_bruttoPerAr = basTp_bruttoPerAr
        this.basPenT_bruttoPerAr = basPenT_bruttoPerAr
    }

}
