package no.nav.pensjon.simulator.core.domain.regler

import java.io.Serializable

/**
 * Trygdetid for EØS-tilfeller.
 */
class TTUtlandEOS(
    /**
     * Framtidig trygdetid EØS i antall måneder.
     */
    var ftt_eos: Int = 0,

    /**
     * Om framtidig trygdetid EØS er redusert. Dersom faktisk trygdetid
     * medregnet tid i Norge og EØS er mindre enn 4/5 av opptjeningstiden skal
     * framtidig trygdetid for EØS beregnes med reduksjon.
     */
    var ftt_eos_redusert: Boolean = false,

    /**
     * Teoretisk trygdetid EØS i antall måneder.
     */
    var tt_eos_anv_mnd: Int = 0,

    /**
     * Teoretisk trygdetid EØS i antall år.
     */
    var tt_eos_anv_ar: Int = 0,

    /**
     * Pro-rata trygdetid i EØS land utenfor Norge i antall måneder.
     */
    var tt_eos_pro_rata_mnd: Int = 0,

    /**
     * Teoretisk trygdetid i EØS land utenfor Norge i antall måneder.
     */
    var tt_eos_teoretisk_mnd: Int = 0,

    /**
     * Teller i EØS pro-rata brøk, i antall måneder.
     */
    var tt_eos_teller: Int = 0,

    /**
     * Nevner i EØS pro-rata brøk, i antall måneder.
     */
    var tt_eos_nevner: Int = 0,
    // usikker på om disse skal ligge begge steder
    /**
     * Trygdetid skal være lik antall poengår. Har bare betydning for personer
     * som har vært bosatt i utlandet.
     */
    var tt_lik_pa: Boolean = false,

    /**
     * Trygdetiden settes lik antall år som blir tastet inn i feltet.
     */
    var tt_konvensjon_ar: Int = 0,

    var merknadListe: MutableList<Merknad> = mutableListOf()
) : Serializable {
    /**
     * Copy Constructor
     */
    constructor(tTUtlandEOS: TTUtlandEOS) : this() {
        this.ftt_eos = tTUtlandEOS.ftt_eos
        this.ftt_eos_redusert = tTUtlandEOS.ftt_eos_redusert
        this.tt_eos_anv_mnd = tTUtlandEOS.tt_eos_anv_mnd
        this.tt_eos_anv_ar = tTUtlandEOS.tt_eos_anv_ar
        this.tt_eos_pro_rata_mnd = tTUtlandEOS.tt_eos_pro_rata_mnd
        this.tt_eos_teoretisk_mnd = tTUtlandEOS.tt_eos_teoretisk_mnd
        this.tt_eos_teller = tTUtlandEOS.tt_eos_teller
        this.tt_eos_nevner = tTUtlandEOS.tt_eos_nevner
        this.tt_lik_pa = tTUtlandEOS.tt_lik_pa
        this.tt_konvensjon_ar = tTUtlandEOS.tt_konvensjon_ar
        this.merknadListe = mutableListOf()
        for (merknad in tTUtlandEOS.merknadListe) {
            this.merknadListe.add(Merknad(merknad))
        }
    }

    constructor(
        ftt_eos: Int,
        ftt_eos_redusert: Boolean,
        tt_eos_anv_mnd: Int,
        tt_eos_anv_ar: Int,
        tt_eos_pro_rata_mnd: Int,
        tt_eos_teoretisk_mnd: Int,
        tt_eos_teller: Int,
        tt_eos_nevner: Int,
        tt_lik_pa: Boolean,
        tt_konvensjon_ar: Int
    ) : this() {
        this.ftt_eos = ftt_eos
        this.ftt_eos_redusert = ftt_eos_redusert
        this.tt_eos_anv_mnd = tt_eos_anv_mnd
        this.tt_eos_anv_ar = tt_eos_anv_ar
        this.tt_eos_pro_rata_mnd = tt_eos_pro_rata_mnd
        this.tt_eos_teoretisk_mnd = tt_eos_teoretisk_mnd
        this.tt_eos_teller = tt_eos_teller
        this.tt_eos_nevner = tt_eos_nevner
        this.tt_lik_pa = tt_lik_pa
        this.tt_konvensjon_ar = tt_konvensjon_ar
    }
}
