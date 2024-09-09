package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class SatsTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(typeCti: TypeCti?) : super(typeCti!!)
    constructor(kode: String) : super(kode)
}
