package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class InntektKode2Cti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(inntektKode2Cti: InntektKode2Cti?) : super(inntektKode2Cti!!)
}
