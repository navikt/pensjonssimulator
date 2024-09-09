package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class AvkortingsArsakCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(avkortingsArsakCti: AvkortingsArsakCti?) : super(avkortingsArsakCti!!)
}
