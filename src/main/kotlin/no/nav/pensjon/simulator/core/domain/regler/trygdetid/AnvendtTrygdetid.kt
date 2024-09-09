package no.nav.pensjon.simulator.core.domain.regler.trygdetid

import java.io.Serializable

/**
 * Anvendt trygdetid i beregning av grunnpensjon med mer.
 */
class AnvendtTrygdetid : Serializable {
    /**
     * Anvendt trygdetid i antall år.
     */
    var tt_anv = 0

    /**
     * Anvendt pro rata brøk hvis grunnpensjon er pro rata beregnet.
     * Teller er lik antall måneder faktisk trygdetid i Norge.
     * Nevner er lik antall måneder faktisk trygdetid i Norge og i avtaleland.
     */
    var pro_rata: Brok? = null

    constructor() : super()
    constructor(tt_anv: Int = 0, pro_rata: Brok? = null) : this() {
        this.tt_anv = tt_anv
        this.pro_rata = pro_rata
    }

    constructor(anvendtTrygdetid: AnvendtTrygdetid) : this() {
        tt_anv = anvendtTrygdetid.tt_anv
        if (anvendtTrygdetid.pro_rata != null) {
            pro_rata = Brok(anvendtTrygdetid.pro_rata!!)
        }
    }
}
