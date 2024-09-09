package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class JustertGarantipensjonsnivaTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(t: JustertGarantipensjonsnivaTypeCti?) : super(t!!)
}
