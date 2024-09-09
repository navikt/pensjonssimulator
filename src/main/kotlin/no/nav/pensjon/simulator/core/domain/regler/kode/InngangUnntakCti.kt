package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class InngangUnntakCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(inngangUnntakCti: InngangUnntakCti?) : super(inngangUnntakCti!!)
}
