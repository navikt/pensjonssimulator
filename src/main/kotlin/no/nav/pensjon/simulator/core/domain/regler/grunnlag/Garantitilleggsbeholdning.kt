package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.GarantitilleggInformasjon
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.BeholdningsTypeCti

// Checked 2025-02-28
class Garantitilleggsbeholdning : Beholdning() {
    var garantitilleggInformasjon: GarantitilleggInformasjon? = null
    override var beholdningsType: BeholdningsTypeCti = BeholdningsTypeCti("GAR_T_B")
    override var beholdningsTypeEnum: BeholdningtypeEnum = BeholdningtypeEnum.GAR_T_B
}
