package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class InntektKode1Cti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(inntektKode1Cti: InntektKode1Cti?) : super(inntektKode1Cti!!)
}
