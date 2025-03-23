package no.nav.pensjon.simulator.core.domain.regler

import no.nav.pensjon.simulator.core.domain.reglerextend.copy
import java.io.Serializable

/**
 * Trygdetid for utenlandssaker, men ikke EØS. Dvs nordisk konvensjon (artikkel
 * 10) og andre land med bilaterale avtaler.
 */
class TTUtlandKonvensjon(
    /**
     * Brutto framtidig trygdetid i antall måneder etter Nordisk konvensjon.
     */
    var ftt_A10_brutto: Int = 0,

    /**
     * Netto framtidig trygdetid i antall måneder etter Nordisk konvensjon.
     */
    var ftt_A10_netto: Int = 0,

    /**
     * Om framtidig trygdetid etter Nordisk konvensjon er redusert etter
     * 4/5-dels regel.
     */
    var ftt_A10_redusert: Boolean = false,

    /**
     * Antall framtidige år, brukes ved bilaterale avtaler med UK og Nederland
     */
    var ft_ar: Int = 0,

    /**
     * Faktisk trygdetid i antall måneder etter Nordisk konvensjon.
     */
    var tt_A10_fa_mnd: Int = 0,

    /**
     * Faktisk trygdetid i antall år etter Nordisk konvensjon.
     */
    var tt_A10_anv_aar: Int = 0,

    /**
     * Teller i Nordisk pro-rata brøk.
     */
    var tt_A10_teller: Int = 0,

    /**
     * Nevner i Nordisk pro-rata brøk.
     */
    var tt_A10_nevner: Int = 0,

    /**
     * Trygdetiden settes lik antall år som blir tastet inn i feltet.
     */
    var tt_konvensjon_ar: Int = 0,

    /**
     * Trygdetid skal være like antall poeng år. Har bare betydning for personer
     * som har vært bosatt i utlandet.
     */
    var tt_lik_pa: Boolean = false,

    var merknadListe: MutableList<Merknad> = mutableListOf()
) : Serializable {
    /**
     * Copy Constructor
     */
    constructor(tTUtlandKonvensjon: TTUtlandKonvensjon) : this() {
        this.ftt_A10_brutto = tTUtlandKonvensjon.ftt_A10_brutto
        this.ftt_A10_netto = tTUtlandKonvensjon.ftt_A10_netto
        this.ftt_A10_redusert = tTUtlandKonvensjon.ftt_A10_redusert
        this.ft_ar = tTUtlandKonvensjon.ft_ar
        this.tt_A10_fa_mnd = tTUtlandKonvensjon.tt_A10_fa_mnd
        this.tt_A10_anv_aar = tTUtlandKonvensjon.tt_A10_anv_aar
        this.tt_A10_teller = tTUtlandKonvensjon.tt_A10_teller
        this.tt_A10_nevner = tTUtlandKonvensjon.tt_A10_nevner
        this.tt_konvensjon_ar = tTUtlandKonvensjon.tt_konvensjon_ar
        this.tt_lik_pa = tTUtlandKonvensjon.tt_lik_pa
        this.merknadListe = mutableListOf()
        this.merknadListe = tTUtlandKonvensjon.merknadListe.map { it.copy() }.toMutableList()
    }
}
