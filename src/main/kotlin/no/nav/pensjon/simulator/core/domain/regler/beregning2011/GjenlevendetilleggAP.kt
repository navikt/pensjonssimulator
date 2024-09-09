package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.YtelsekomponentTypeCti

/**
 * GjenlevendetilleggAP
 *
 * @author Lars Hartvigsen (Decisive), PK-20265
 * @author Magnus Bakken (Accenture), PK-20716
 */
class GjenlevendetilleggAP : Ytelseskomponent {

    /**
     * Sum av GP, TP og PenT for AP2011 medregnet GJR.
     */
    var apKap19MedGJR: Int = 0

    /**
     * Sum av GP, TP og PenT for AP2011 uten GJR.
     */
    var apKap19UtenGJR: Int = 0

    constructor() : super(ytelsekomponentType = YtelsekomponentTypeCti("AP_GJT"))

    constructor(ytelseskomponent: GjenlevendetilleggAP) : super(ytelseskomponent) {
        this.apKap19MedGJR = ytelseskomponent.apKap19MedGJR
        this.apKap19UtenGJR = ytelseskomponent.apKap19UtenGJR
    }

    constructor(
            apKap19MedGJR: Int = 0,
            apKap19UtenGJR: Int = 0,
            /** super Ytelseskomponent*/
            brutto: Int = 0,
            netto: Int = 0,
            fradrag: Int = 0,
            bruttoPerAr: Double = 0.0,
            nettoPerAr: Double = 0.0,
            fradragPerAr: Double = 0.0,
            ytelsekomponentType: YtelsekomponentTypeCti = YtelsekomponentTypeCti("AP_GJT"),
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
        this.apKap19MedGJR = apKap19MedGJR
        this.apKap19UtenGJR = apKap19UtenGJR
    }
}
