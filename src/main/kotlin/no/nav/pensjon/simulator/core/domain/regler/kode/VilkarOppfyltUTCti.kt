package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

/**
 * Beskriver hvorvidt saksbehandler mener vilkår er oppfylt.
 */
class VilkarOppfyltUTCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(vilkarOppfyltUTCti: VilkarOppfyltUTCti?) : super(vilkarOppfyltUTCti!!)
}
