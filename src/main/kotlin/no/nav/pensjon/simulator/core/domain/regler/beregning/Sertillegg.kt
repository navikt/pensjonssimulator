package no.nav.pensjon.simulator.core.domain.regler.beregning

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

class Sertillegg : Ytelseskomponent {

    /**
     * Prosentsatsen
     */
    var pSats_st: Double = 0.0

    @JsonIgnore
    var orginalBrutto: Int = 0

    @JsonIgnore
    var orginalBruttoPerAr: Double = 0.0

    constructor(sertillegg: Sertillegg) : super(sertillegg) {
        pSats_st = sertillegg.pSats_st
        orginalBrutto = sertillegg.orginalBrutto
        orginalBruttoPerAr = sertillegg.orginalBruttoPerAr
    }

    constructor() : super(
        ytelsekomponentType = YtelsekomponentTypeCti("ST")
    )

    constructor(
        pSats_st: Double = 0.0,
        orginalBrutto: Int = 0,
        orginalBruttoPerAr: Double = 0.0,
        /** super Ytelseskomponent*/
        brutto: Int = 0,
        netto: Int = 0,
        fradrag: Int = 0,
        bruttoPerAr: Double = 0.0,
        nettoPerAr: Double = 0.0,
        fradragPerAr: Double = 0.0,
        ytelsekomponentType: YtelsekomponentTypeCti = YtelsekomponentTypeCti("ST"),
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
        this.pSats_st = pSats_st
        this.orginalBrutto = orginalBrutto
        this.orginalBruttoPerAr = orginalBruttoPerAr
    }
}
