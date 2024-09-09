package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

/**
 * Ventetillegg. netto=brutto=venteTillegg_GP+venteTillegg_TP
 */
class Ventetillegg : Ytelseskomponent {

    /**
     * Ventetillegget for GP
     */
    var venteTillegg_GP: Int = 0

    /**
     * Ventetillegget for tillegspensjon
     */
    var venteTillegg_TP: Int = 0

    /**
     * Prosenten
     */
    var venteTilleggProsent: Double = 0.0

    constructor() : super(ytelsekomponentType = YtelsekomponentTypeCti("VT"))

    constructor(ventetillegg: Ventetillegg) : super(ventetillegg) {
        venteTillegg_GP = ventetillegg.venteTillegg_GP
        venteTillegg_TP = ventetillegg.venteTillegg_TP
        venteTilleggProsent = ventetillegg.venteTilleggProsent
    }

    constructor(
        venteTillegg_GP: Int = 0,
        venteTillegg_TP: Int = 0,
        venteTilleggProsent: Double = 0.0,
        /** super Ytelseskomponent*/
        brutto: Int = 0,
        netto: Int = 0,
        fradrag: Int = 0,
        bruttoPerAr: Double = 0.0,
        nettoPerAr: Double = 0.0,
        fradragPerAr: Double = 0.0,
        ytelsekomponentType: YtelsekomponentTypeCti = YtelsekomponentTypeCti("VT"),
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
        this.venteTillegg_GP = venteTillegg_GP
        this.venteTillegg_TP = venteTillegg_TP
        this.venteTilleggProsent = venteTilleggProsent
    }
}
