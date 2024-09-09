package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class AvtaleKritCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(avtaleKritCti: AvtaleKritCti?) : super(avtaleKritCti!!)
}
