package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class VilkarsvedtakTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(vilkarsvedtakTypeCti: VilkarsvedtakTypeCti?) : super(vilkarsvedtakTypeCti!!)
}
