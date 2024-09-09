package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class EksportUnntakCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(eksportUnntakCti: EksportUnntakCti?) : super(eksportUnntakCti!!)
}
