package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class BarnepensjonEOSKapittelCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(barnepensjonEOSKapittelCti: BarnepensjonEOSKapittelCti?) : super(barnepensjonEOSKapittelCti!!)
}
