package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class GrunnlagsrolleCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(grunnlagsrolleCti: GrunnlagsrolleCti?) : super(grunnlagsrolleCti!!)
}
