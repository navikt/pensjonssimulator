package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class MinstepenNivaCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(minstepensjonTypeCti: MinstepenNivaCti?) : super(minstepensjonTypeCti!!)
}
