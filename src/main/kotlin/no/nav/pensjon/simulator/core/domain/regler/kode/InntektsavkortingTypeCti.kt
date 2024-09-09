package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class InntektsavkortingTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(inntektsavkortingTypeCti: InntektsavkortingTypeCti?) : super(inntektsavkortingTypeCti!!)
}
