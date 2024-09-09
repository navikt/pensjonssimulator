package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class OpptjeningTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(opptjeningTypeCti: OpptjeningTypeCti?) : super(opptjeningTypeCti!!)
}
