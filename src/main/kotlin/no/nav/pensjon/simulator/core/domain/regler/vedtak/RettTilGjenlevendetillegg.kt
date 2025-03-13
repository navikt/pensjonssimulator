package no.nav.pensjon.simulator.core.domain.regler.vedtak

// 2025-03-10
class RettTilGjenlevendetillegg : AbstraktVilkar() {
    /**
     * Angir om gjenlevendetillegget skal beregnes som konvertert eller ikke.
     */
    var konvertert = false
/*
    constructor() : super()
    constructor(rettTilGjenlevendetillegg: RettTilGjenlevendetillegg?) : super(rettTilGjenlevendetillegg!!) {
        this.konvertert = rettTilGjenlevendetillegg.konvertert
    }
    */
}
