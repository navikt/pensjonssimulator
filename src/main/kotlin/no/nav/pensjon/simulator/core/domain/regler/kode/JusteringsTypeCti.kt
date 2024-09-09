package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class JusteringsTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(t: JusteringsTypeCti?) : super(t!!)
}
