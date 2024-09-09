package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class InntektTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(inntektTypeCti: InntektTypeCti?) : super(inntektTypeCti!!)
}
