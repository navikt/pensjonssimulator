package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.GarantitilleggInformasjon
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum

// Copied from pensjon-regler-api v2.4.3 2026-09-04
class Garantitilleggsbeholdning : Beholdning() {
    var garantitilleggInformasjon: GarantitilleggInformasjon? = null
    override var beholdningsTypeEnum: BeholdningtypeEnum = BeholdningtypeEnum.GAR_T_B
}