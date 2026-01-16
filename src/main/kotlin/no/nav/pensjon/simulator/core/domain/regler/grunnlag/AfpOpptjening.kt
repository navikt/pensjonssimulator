package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum

// Copied from pensjon-regler-api 2026-01-16
class AfpOpptjening : Beholdning() {
    override var beholdningsTypeEnum: BeholdningtypeEnum = BeholdningtypeEnum.AFP
}
