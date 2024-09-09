package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class AvtalelandCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(avtalelandCti: AvtalelandCti?) : super(avtalelandCti!!)

}
