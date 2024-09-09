package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class VarighetTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(varighetTypeCti: VarighetTypeCti?) : super(varighetTypeCti!!)
}
