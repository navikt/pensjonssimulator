package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class AvviksjusteringCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(avviksjusteringCti: AvviksjusteringCti?) : super(avviksjusteringCti!!)
}
