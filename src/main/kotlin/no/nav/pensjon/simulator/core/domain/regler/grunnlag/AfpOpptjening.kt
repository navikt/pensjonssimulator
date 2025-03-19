package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum

// 2025-03-19
class AfpOpptjening : Beholdning() {
    override var beholdningsTypeEnum: BeholdningtypeEnum = BeholdningtypeEnum.AFP
}
