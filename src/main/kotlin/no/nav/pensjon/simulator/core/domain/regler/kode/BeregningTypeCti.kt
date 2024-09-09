package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class BeregningTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(beregningTypeCti: BeregningTypeCti?) : super(beregningTypeCti!!)
}
