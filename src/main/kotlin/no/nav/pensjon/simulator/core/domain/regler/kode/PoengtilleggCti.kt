package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class PoengtilleggCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(yrkeCti: PoengtilleggCti?) : super(yrkeCti!!)
}
