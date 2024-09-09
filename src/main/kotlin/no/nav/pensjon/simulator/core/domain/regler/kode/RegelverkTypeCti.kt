package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class RegelverkTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(regelverkTypeCti: RegelverkTypeCti?) : super(regelverkTypeCti!!)
}
