package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class AvtaleDatoCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(avtaleDatoCti: AvtaleDatoCti?) : super(avtaleDatoCti!!)
}
