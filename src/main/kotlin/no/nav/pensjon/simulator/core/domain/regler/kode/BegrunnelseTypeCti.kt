package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class BegrunnelseTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
}
