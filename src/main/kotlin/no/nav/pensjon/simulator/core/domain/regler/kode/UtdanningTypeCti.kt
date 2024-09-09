package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class UtdanningTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(utdanningTypeCti: UtdanningTypeCti?) : super(utdanningTypeCti!!)
}
