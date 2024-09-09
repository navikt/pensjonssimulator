package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

class GrunnlagKildeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(grunnlagKildeCti: GrunnlagKildeCti?) : super(grunnlagKildeCti!!)
}
