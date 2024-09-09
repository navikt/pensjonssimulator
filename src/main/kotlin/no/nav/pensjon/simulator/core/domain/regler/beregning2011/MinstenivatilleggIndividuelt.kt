package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

class MinstenivatilleggIndividuelt : Ytelseskomponent {
    var mpn: MinstePensjonsniva? = null
    var garPN: Garantipensjonsniva? = null
    var samletPensjonForMNT: Double = 0.0

    constructor() : super(ytelsekomponentType = YtelsekomponentTypeCti("MIN_NIVA_TILL_INDV"))

    constructor(mnt: MinstenivatilleggIndividuelt) : super(mnt) {
        samletPensjonForMNT = mnt.samletPensjonForMNT
        if (mnt.mpn != null) {
            mpn = MinstePensjonsniva(mnt.mpn!!)
        }
        if (mnt.garPN != null) {
            garPN = Garantipensjonsniva(mnt.garPN!!)
        }
    }

    constructor(
            mpn: MinstePensjonsniva? = null,
            garPN: Garantipensjonsniva? = null,
            samletPensjonForMNT: Double = 0.0,
            /** super Ytelseskomponent*/
            brutto: Int = 0,
            netto: Int = 0,
            fradrag: Int = 0,
            bruttoPerAr: Double = 0.0,
            nettoPerAr: Double = 0.0,
            fradragPerAr: Double = 0.0,
            ytelsekomponentType: YtelsekomponentTypeCti = YtelsekomponentTypeCti("MIN_NIVA_TILL_INDV"),
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
        this.mpn = mpn
        this.garPN = garPN
        this.samletPensjonForMNT = samletPensjonForMNT
    }
}
