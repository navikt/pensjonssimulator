package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.BeholdningsTypeCti

// Checked 2025-02-28
class AfpOpptjening : Beholdning {
    override var beholdningsType: BeholdningsTypeCti = BeholdningsTypeCti("AFP")
    override var beholdningsTypeEnum: BeholdningtypeEnum = BeholdningtypeEnum.AFP

    constructor() : super() {
        beholdningsType = BeholdningsTypeCti("AFP")
    }

    constructor(source: AfpOpptjening) : super(source)
}
