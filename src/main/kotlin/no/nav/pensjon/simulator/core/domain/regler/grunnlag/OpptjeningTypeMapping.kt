package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.OpptjeningPOPPTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.OpptjeningPOPPTypeCti

// Checked 2025-02-28
class OpptjeningTypeMapping {
    var opptjeningPOPPTypeCti: OpptjeningPOPPTypeCti? = null
    var opptjeningPOPPTypeEnum: OpptjeningPOPPTypeEnum? = null

    constructor()

    constructor(source: OpptjeningTypeMapping) : this() {
        source.opptjeningPOPPTypeCti?.let { OpptjeningPOPPTypeCti(it) }
        opptjeningPOPPTypeEnum = source.opptjeningPOPPTypeEnum
    }
}
