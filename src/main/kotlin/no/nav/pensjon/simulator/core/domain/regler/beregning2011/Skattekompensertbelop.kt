package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum

// 2025-03-10
class Skattekompensertbelop {
    var faktor = 0.0
    var formelKodeEnum: FormelKodeEnum? = null
    var arsbelop = 0.0
    var justertbelop: Justertbelop? = null
    var tillegg = 0.0

    constructor() : super() {}

    constructor(source: Skattekompensertbelop) {
        faktor = source.faktor
        formelKodeEnum = source.formelKodeEnum
        arsbelop = source.arsbelop
        justertbelop = source.justertbelop?.let(::Justertbelop)
        tillegg = source.tillegg
    }
}
