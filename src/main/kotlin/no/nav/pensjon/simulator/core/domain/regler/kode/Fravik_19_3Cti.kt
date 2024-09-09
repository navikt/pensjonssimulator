package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class Fravik_19_3Cti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(fravik_19_3Cti: Fravik_19_3Cti?) : super(fravik_19_3Cti!!)

}
