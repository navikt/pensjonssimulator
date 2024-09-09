package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.kode.OpptjeningPOPPTypeCti

class OpptjeningTypeMapping(var opptjeningPOPPTypeCti: OpptjeningPOPPTypeCti? = null) {

    constructor(opptjeningTypeMapping: OpptjeningTypeMapping) : this() {
        if (opptjeningTypeMapping.opptjeningPOPPTypeCti != null) {
            this.opptjeningPOPPTypeCti = OpptjeningPOPPTypeCti(opptjeningTypeMapping.opptjeningPOPPTypeCti)
        }
    }
}
