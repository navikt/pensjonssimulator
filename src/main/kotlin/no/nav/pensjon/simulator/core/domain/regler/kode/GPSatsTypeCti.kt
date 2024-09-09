package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class GPSatsTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(grunnlagsrolleCti: GPSatsTypeCti?) : super(grunnlagsrolleCti!!)
}
