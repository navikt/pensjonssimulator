package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class OpphorsTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(opphorsTypeCti: OpphorsTypeCti?) : super(opphorsTypeCti!!)
}
