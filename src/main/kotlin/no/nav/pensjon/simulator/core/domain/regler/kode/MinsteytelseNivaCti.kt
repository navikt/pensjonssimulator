package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class MinsteytelseNivaCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(minsteytelseNivaCti: MinsteytelseNivaCti?) : super(minsteytelseNivaCti!!)
}
