package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class InstReduksjonCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(instReduksjonCti: InstReduksjonCti?) : super(instReduksjonCti!!)
}
