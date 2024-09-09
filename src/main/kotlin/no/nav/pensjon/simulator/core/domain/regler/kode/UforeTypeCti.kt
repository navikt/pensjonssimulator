package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class UforeTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(uforeTypeCti: UforeTypeCti?) : super(uforeTypeCti!!)
}
