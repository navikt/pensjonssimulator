package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class OppholdTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(oppholdTypeCti: OppholdTypeCti?) : super(oppholdTypeCti!!)
}
