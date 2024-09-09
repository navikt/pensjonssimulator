package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Eksportrett
import no.nav.pensjon.simulator.core.domain.regler.kode.VilkarOppfyltUTCti

class RettTilEksportEtterTrygdeavtaler : AbstraktVilkar {
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

    constructor(
        eksportrettEtterEOSForordning: Eksportrett?,
        eksportrettEtterTrygdeavtalerEOS: Eksportrett?,
        eksportrettEtterAndreTrygdeavtaler: Eksportrett?
    ) {
        this.eksportrettEtterEOSForordning = eksportrettEtterEOSForordning
        this.eksportrettEtterTrygdeavtalerEOS = eksportrettEtterTrygdeavtalerEOS
        this.eksportrettEtterAndreTrygdeavtaler = eksportrettEtterAndreTrygdeavtaler
    }

    constructor(
        resultat: VilkarOppfyltUTCti? = null,
        /** Interne felt */
        eksportrettEtterEOSForordning: Eksportrett? = null,
        eksportrettEtterTrygdeavtalerEOS: Eksportrett? = null,
        eksportrettEtterAndreTrygdeavtaler: Eksportrett? = null
    ) : super(resultat) {
        this.eksportrettEtterEOSForordning = eksportrettEtterEOSForordning
        this.eksportrettEtterTrygdeavtalerEOS = eksportrettEtterTrygdeavtalerEOS
        this.eksportrettEtterAndreTrygdeavtaler = eksportrettEtterAndreTrygdeavtaler
    }

    override fun dypKopi(abstraktVilkar: AbstraktVilkar): AbstraktVilkar? {
        var rte: RettTilEksportEtterTrygdeavtaler? = null
        if (abstraktVilkar.javaClass == RettTilEksportEtterTrygdeavtaler::class.java) {
            rte = RettTilEksportEtterTrygdeavtaler(abstraktVilkar as RettTilEksportEtterTrygdeavtaler?)
        }
        return rte
    }
}
