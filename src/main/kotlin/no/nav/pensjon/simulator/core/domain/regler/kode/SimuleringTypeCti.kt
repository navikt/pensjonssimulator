package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class SimuleringTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(simuleringTypeCti: SimuleringTypeCti?) : super(simuleringTypeCti!!)
}
