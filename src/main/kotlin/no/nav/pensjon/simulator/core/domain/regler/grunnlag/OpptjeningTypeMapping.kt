package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.OpptjeningPOPPTypeEnum

// Checked 2025-02-28
class OpptjeningTypeMapping {
    var opptjeningPOPPTypeEnum: OpptjeningPOPPTypeEnum? = null

    constructor()

    constructor(source: OpptjeningTypeMapping) : this() {
        opptjeningPOPPTypeEnum = source.opptjeningPOPPTypeEnum
    }
}
