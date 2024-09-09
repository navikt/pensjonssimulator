package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class BorMedTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(borMedTypeCti: BorMedTypeCti?) : super(borMedTypeCti!!)
}
