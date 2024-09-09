package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class BeholdningsTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(t: BeholdningsTypeCti?) : super(t!!)
}
