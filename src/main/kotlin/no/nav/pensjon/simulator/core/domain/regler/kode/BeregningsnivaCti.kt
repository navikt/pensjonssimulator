package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class BeregningsnivaCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(beregningsnivaCti: BeregningsnivaCti?) : super(beregningsnivaCti!!)
}
