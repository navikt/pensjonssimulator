package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class BarnekullTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(barnekullTypeCti: BarnekullTypeCti?) : super(barnekullTypeCti!!)
}
