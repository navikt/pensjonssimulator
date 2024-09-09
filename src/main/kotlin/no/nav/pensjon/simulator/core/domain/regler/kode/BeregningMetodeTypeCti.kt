package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class BeregningMetodeTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(beregningMetodeTypeCti: BeregningMetodeTypeCti?) : super(beregningMetodeTypeCti!!)

}
