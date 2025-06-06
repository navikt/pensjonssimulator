package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.BarnetilleggVilkarTypeEnum

// 2025-06-06
/**
 * Representerer et vilkår for barnetillegg på uføretrygd. Saksbehandler gjår en vurdering av enkeltvilkår.
 */
class BarnetilleggVilkar {
    var btVilkarTypeEnum: BarnetilleggVilkarTypeEnum? = null
    var vurdertTil = false
}
