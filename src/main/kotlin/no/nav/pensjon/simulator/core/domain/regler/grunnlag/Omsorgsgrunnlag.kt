package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.OmsorgTypeEnum

// Checked 2025-02-28
class Omsorgsgrunnlag {
    var ar = 0
    var omsorgTypeEnum: OmsorgTypeEnum? = null
    var personOmsorgFor: PenPerson? = null
    var bruk = false

    constructor()

    constructor(source: Omsorgsgrunnlag) : this() {
        ar = source.ar
        omsorgTypeEnum = source.omsorgTypeEnum
        personOmsorgFor = source.personOmsorgFor?.let(::PenPerson)
        bruk = source.bruk
    }
}
