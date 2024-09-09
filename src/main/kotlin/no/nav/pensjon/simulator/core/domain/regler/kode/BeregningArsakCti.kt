package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class BeregningArsakCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(beregningArsakCti: BeregningArsakCti?) : super(beregningArsakCti!!)
}
