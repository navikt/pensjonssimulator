package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class YtelsekomponentTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(ytelseTypeCti: YtelsekomponentTypeCti?) : super(ytelseTypeCti!!)

}
