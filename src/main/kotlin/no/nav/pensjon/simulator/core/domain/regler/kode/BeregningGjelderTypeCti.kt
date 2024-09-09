package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class BeregningGjelderTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(typeCti: TypeCti?) : super(typeCti!!)

}
