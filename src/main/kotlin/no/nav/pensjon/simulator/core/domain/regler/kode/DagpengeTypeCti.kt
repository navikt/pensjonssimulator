package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class DagpengeTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(t: DagpengeTypeCti?) : super(t!!)
}
