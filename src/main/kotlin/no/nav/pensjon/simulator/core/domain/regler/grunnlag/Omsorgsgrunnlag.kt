package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.kode.OmsorgTypeCti

class Omsorgsgrunnlag(
    var ar: Int = 0,
    var omsorgType: OmsorgTypeCti? = null,
    var personOmsorgFor: PenPerson? = null,
    var bruk: Boolean = false
) {
    constructor(og: Omsorgsgrunnlag) : this() {
        this.ar = og.ar
        if (og.omsorgType != null) {
            this.omsorgType = OmsorgTypeCti(og.omsorgType)
        }
        if (og.personOmsorgFor != null) {
            this.personOmsorgFor = PenPerson(og.personOmsorgFor!!)
        }
        this.bruk = og.bruk
    }
}
