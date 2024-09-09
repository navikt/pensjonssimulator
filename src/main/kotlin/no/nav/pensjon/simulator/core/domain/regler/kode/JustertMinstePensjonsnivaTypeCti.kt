package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class JustertMinstePensjonsnivaTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(fremskrevetMPNTypeCti: JustertMinstePensjonsnivaTypeCti?) : super(fremskrevetMPNTypeCti!!)
}
