package no.nav.pensjon.simulator.opptjening.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum

/**
 * NB: This class must be named 'AfpOpptjening', due to API constraints.
 */
class AfpOpptjening : PenBeholdning() {
    override var beholdningsTypeEnum: BeholdningtypeEnum = BeholdningtypeEnum.AFP
}