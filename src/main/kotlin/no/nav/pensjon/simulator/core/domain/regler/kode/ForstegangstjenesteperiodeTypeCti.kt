package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class ForstegangstjenesteperiodeTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(avtaleTypeCti: ForstegangstjenesteperiodeTypeCti?) : super(avtaleTypeCti!!)

}
