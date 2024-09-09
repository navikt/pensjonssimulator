package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.kode.BarnetilleggVilkarTypeCti
import java.io.Serializable

class BarnetilleggVilkar(
    var btVilkarType: BarnetilleggVilkarTypeCti? = null,
    var vurdertTil: Boolean = false
) : Serializable {

    constructor(b: BarnetilleggVilkar) : this() {
        this.btVilkarType = b.btVilkarType
        this.vurdertTil = b.vurdertTil
    }
}
