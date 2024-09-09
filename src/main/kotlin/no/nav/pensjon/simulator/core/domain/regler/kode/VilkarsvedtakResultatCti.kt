package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class VilkarsvedtakResultatCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(vilkarsvedtakResultatCti: VilkarsvedtakResultatCti?) : super(vilkarsvedtakResultatCti!!)
}
