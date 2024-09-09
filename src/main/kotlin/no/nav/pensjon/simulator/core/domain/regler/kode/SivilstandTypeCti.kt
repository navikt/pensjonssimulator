package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class SivilstandTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(sivilstandTypeCti: SivilstandTypeCti?) : super(sivilstandTypeCti!!)
}
