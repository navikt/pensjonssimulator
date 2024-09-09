package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.kode.VilkarOppfyltUTCti

class RettTilGjenlevendetillegg : AbstraktVilkar {
    /**
     * Angir om gjenlevendetillegget skal beregnes som konvertert eller ikke.
     */
    var konvertert = false

    constructor() : super()
    constructor(rettTilGjenlevendetillegg: RettTilGjenlevendetillegg?) : super(rettTilGjenlevendetillegg!!) {
        this.konvertert = rettTilGjenlevendetillegg.konvertert
    }

    constructor(
        resultat: VilkarOppfyltUTCti? = null,
        /** Interne felt */
        konvertert: Boolean = false
    ) : super(resultat) {
        this.konvertert = konvertert
    }

    override fun dypKopi(abstraktVilkar: AbstraktVilkar): AbstraktVilkar? {
        var rettTilGjenlevendetillegg: RettTilGjenlevendetillegg? = null
        if (abstraktVilkar.javaClass == RettTilGjenlevendetillegg::class.java) {
            rettTilGjenlevendetillegg = RettTilGjenlevendetillegg(abstraktVilkar as RettTilGjenlevendetillegg?)
        }
        return rettTilGjenlevendetillegg
    }

}
