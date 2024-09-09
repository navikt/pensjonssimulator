package no.nav.pensjon.simulator.core.domain.regler.kode

import java.io.Serializable

/**
 * Inntektstyper som POPP kjenner, bruker kodeverket K_OPPTJN_POPP_T
 */
class OpptjeningPOPPTypeCti : TypeCti, Serializable {
    constructor() : super("")
    constructor(kode: String) : super(kode)
    constructor(opptjeningPOPPTypeCti: OpptjeningPOPPTypeCti?) : super(opptjeningPOPPTypeCti!!)

}
