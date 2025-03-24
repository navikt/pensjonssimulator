package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum

// Checked 2025-02-28
class AfpOpptjening : Beholdning {
    override var beholdningsTypeEnum: BeholdningtypeEnum = BeholdningtypeEnum.AFP

    constructor() : super() {
        beholdningsTypeEnum = BeholdningtypeEnum.AFP
    }

    constructor(source: AfpOpptjening) : super(source)
}
