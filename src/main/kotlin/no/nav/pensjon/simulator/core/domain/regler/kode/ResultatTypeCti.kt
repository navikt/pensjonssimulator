package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class ResultatTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(resultatTypeCti: ResultatTypeCti?) : super(resultatTypeCti!!)
}
