package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class AfpOrdningTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(afpOrdningTypeCti: AfpOrdningTypeCti?) : super(afpOrdningTypeCti!!)
}
