package no.nav.pensjon.simulator.core.domain.regler

import no.nav.pensjon.simulator.core.domain.regler.kode.AvtalelandCti
import java.io.Serializable

/**
 * Trygdetid for land med bilaterale avtaler.
 */
class TTUtlandTrygdeavtale(
    /**
     * Framtidig trygdetid i avtaleland i antall måneder.
     */
    var ftt: Int = 0,

    /**
     * Om framtidig trygdetid er redusert etter 4/5-dels regel.
     */
    var ftt_redusert: Boolean = false,

    /**
     * Faktisk trygdetid i avtaleland i antall måneder.
     */
    var tt_fa_mnd: Int = 0,

    /**
     * Anvendt trygdetid i avtaleland i antall år.
     */
    var tt_anv_ar: Int = 0,

    /**
     * Anvendt trygdetid i avtaleland i antall måneder.
     */
    var tt_anv_mnd: Int = 0,

    /**
     * Teller i pro-rata brøk.
     */
    var pro_rata_teller: Int = 0,

    /**
     * Nevner i pro-rata brøk.
     */
    var pro_rata_nevner: Int = 0,

    /**
     * Avtaleland som trygdetid er opptjent i.
     */
    var avtaleland: AvtalelandCti? = null,

    var merknadListe: MutableList<Merknad> = mutableListOf()
) : Serializable {

    constructor(tTUtlandTrygdeavtale: TTUtlandTrygdeavtale) : this() {
        this.ftt = tTUtlandTrygdeavtale.ftt
        this.ftt_redusert = tTUtlandTrygdeavtale.ftt_redusert
        this.tt_fa_mnd = tTUtlandTrygdeavtale.tt_fa_mnd
        this.tt_anv_ar = tTUtlandTrygdeavtale.tt_anv_ar
        this.tt_anv_mnd = tTUtlandTrygdeavtale.tt_anv_mnd
        this.pro_rata_teller = tTUtlandTrygdeavtale.pro_rata_teller
        this.pro_rata_nevner = tTUtlandTrygdeavtale.pro_rata_nevner
        if (tTUtlandTrygdeavtale.avtaleland != null) {
            this.avtaleland = AvtalelandCti(tTUtlandTrygdeavtale.avtaleland)
        }
        this.merknadListe = mutableListOf()
        for (merknad in tTUtlandTrygdeavtale.merknadListe) {
            this.merknadListe.add(Merknad(merknad))
        }
    }
}
