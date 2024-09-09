package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class FormelKodeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(formelKode: FormelKodeCti) : super(formelKode)
}
