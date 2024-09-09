package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class UtfallTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(utfallTypeCti: UtfallTypeCti) : super(utfallTypeCti)
}
