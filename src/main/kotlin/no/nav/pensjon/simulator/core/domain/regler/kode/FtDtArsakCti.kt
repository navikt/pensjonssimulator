package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class FtDtArsakCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(t: FtDtArsakCti?) : super(t!!)
}
