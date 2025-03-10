package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Eksportrett

// 2025-03-10
class RettTilEksportEtterTrygdeavtaler : AbstraktVilkar() {
    /**
     * Eksportrett etter EØS forordning
     */
    var eksportrettEtterEOSForordning: Eksportrett? = null

    /**
     * Eksportrett etter trygdeavtaler EØS
     */
    var eksportrettEtterTrygdeavtalerEOS: Eksportrett? = null

    /**
     * Eksportrett etter andre trygdeavtaler.
     */
    var eksportrettEtterAndreTrygdeavtaler: Eksportrett? = null
/*
    constructor() : super()

    constructor(rettTilEksportEtterTrygdeavtaler: RettTilEksportEtterTrygdeavtaler?) : super(
        rettTilEksportEtterTrygdeavtaler!!
    ) {
        if (rettTilEksportEtterTrygdeavtaler.eksportrettEtterEOSForordning != null) {
            this.eksportrettEtterEOSForordning =
                Eksportrett(rettTilEksportEtterTrygdeavtaler.eksportrettEtterEOSForordning!!)
        }
        if (rettTilEksportEtterTrygdeavtaler.eksportrettEtterTrygdeavtalerEOS != null) {
            this.eksportrettEtterTrygdeavtalerEOS =
                Eksportrett(rettTilEksportEtterTrygdeavtaler.eksportrettEtterTrygdeavtalerEOS!!)
        }
        if (rettTilEksportEtterTrygdeavtaler.eksportrettEtterAndreTrygdeavtaler != null) {
            this.eksportrettEtterAndreTrygdeavtaler =
                Eksportrett(rettTilEksportEtterTrygdeavtaler.eksportrettEtterAndreTrygdeavtaler!!)
        }
    }
*/
}
