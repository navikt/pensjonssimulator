package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class YtelseTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(ytelseTypeCti: YtelseTypeCti?) : super(ytelseTypeCti!!)
}
