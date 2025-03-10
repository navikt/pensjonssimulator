package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum

// 2025-03-10
class Overgangsbelop {
    var formelKodeEnum: FormelKodeEnum? = null
    var gpBrutto = 0
    var gpSats = 0.0
    var overgangsbelop = 0.0
    var stBrutto = 0
    var tpBrutto = 0

    constructor() : super() {}

    constructor(source: Overgangsbelop) {
        formelKodeEnum = source.formelKodeEnum
        gpBrutto = source.gpBrutto
        gpSats = source.gpSats
        overgangsbelop = source.overgangsbelop
        stBrutto = source.stBrutto
        tpBrutto = source.tpBrutto
    }
}
