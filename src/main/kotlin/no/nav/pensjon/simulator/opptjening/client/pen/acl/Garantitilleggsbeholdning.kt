package no.nav.pensjon.simulator.opptjening.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.GarantitilleggInformasjon
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum

/**
 * NB: This class must be named 'Garantitilleggsbeholdning', due to API constraints.
 */
class Garantitilleggsbeholdning : PenBeholdning() {
    var garantitilleggInformasjon: GarantitilleggInformasjon? = null
    override var beholdningsTypeEnum: BeholdningtypeEnum = BeholdningtypeEnum.GAR_T_B
}