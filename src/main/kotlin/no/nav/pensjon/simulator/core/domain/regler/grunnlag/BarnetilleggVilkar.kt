package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.BarnetilleggVilkarTypeEnum

/**
 * Representerer et vilkår for barnetillegg på uføretrygd. Saksbehandler gjår en vurdering av enkeltvilkår.
 */
// Checked 2025-02-28
class BarnetilleggVilkar {
    var btVilkarTypeEnum: BarnetilleggVilkarTypeEnum? = null
    var vurdertTil = false

    constructor()

    constructor(source: BarnetilleggVilkar) : this() {
        btVilkarTypeEnum = source.btVilkarTypeEnum
        vurdertTil = source.vurdertTil
    }
}
