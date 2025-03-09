package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.OmsorgTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.OmsorgTypeCti

// Checked 2025-02-28
class Omsorgsgrunnlag {
    var ar = 0
    var omsorgType: OmsorgTypeCti? = null
    var omsorgTypeEnum: OmsorgTypeEnum? = null
    var personOmsorgFor: PenPerson? = null
    var bruk = false

    constructor()

    constructor(source: Omsorgsgrunnlag) : this() {
        ar = source.ar
        source.omsorgType?.let { omsorgType = OmsorgTypeCti(it) }
        omsorgTypeEnum = source.omsorgTypeEnum
        source.personOmsorgFor?.let { personOmsorgFor = PenPerson(it) }
        bruk = source.bruk
    }
}
