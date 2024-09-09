package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class EksportlandCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(eksportlandCti: EksportlandCti?) : super(eksportlandCti!!)
}
