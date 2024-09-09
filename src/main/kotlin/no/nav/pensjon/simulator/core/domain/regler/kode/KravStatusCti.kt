package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class KravStatusCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(kravStatusCti: KravStatusCti?) : super(kravStatusCti!!)
}
