package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class MinstepensjonTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(minstepensjonTypeCti: MinstepensjonTypeCti?) : super(minstepensjonTypeCti!!)
}
