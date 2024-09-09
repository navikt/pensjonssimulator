package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

/**
 * Beskriver hvilken vurdering saksbehandler har lagt til grunn for resultatvurderingen.
 * Se K_VILKAR_VURD_T.
 */
class VilkarVurderingCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(vilkarVurderingCti: VilkarVurderingCti?) : super(vilkarVurderingCti!!)
}
