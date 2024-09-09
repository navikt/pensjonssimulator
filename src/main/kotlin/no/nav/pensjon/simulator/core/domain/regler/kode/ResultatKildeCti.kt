package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class ResultatKildeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(resultatKildeCti: ResultatKildeCti?) : super(resultatKildeCti!!)
}
