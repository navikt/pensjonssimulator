package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class FremskrevetMPNTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(fremskrevetMPNTypeCti: FremskrevetMPNTypeCti?) : super(fremskrevetMPNTypeCti!!)
}
