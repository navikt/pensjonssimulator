package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class PoengtallTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(poengtallTypeCti: PoengtallTypeCti?) : super(poengtallTypeCti!!)
}
