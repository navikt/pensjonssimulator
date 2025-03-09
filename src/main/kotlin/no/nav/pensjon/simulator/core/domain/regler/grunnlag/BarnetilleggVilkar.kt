package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.BarnetilleggVilkarTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.BarnetilleggVilkarTypeCti

/**
 * Representerer et vilkår for barnetillegg på uføretrygd. Saksbehandler gjår en vurdering av enkeltvilkår.
 */
// Checked 2025-02-28
class BarnetilleggVilkar {
    var btVilkarType: BarnetilleggVilkarTypeCti? = null
    var btVilkarTypeEnum: BarnetilleggVilkarTypeEnum? = null
    var vurdertTil = false

    constructor()

    constructor(source: BarnetilleggVilkar) : this() {
        btVilkarType = source.btVilkarType
        btVilkarTypeEnum = source.btVilkarTypeEnum
        vurdertTil = source.vurdertTil
    }
}
