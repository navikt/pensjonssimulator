package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class ProRataBeregningTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(proRataBeregningTypeCti: ProRataBeregningTypeCti?) : super(proRataBeregningTypeCti!!)
}
