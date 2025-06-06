package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum

// 2025-06-06
class Skattekompensertbelop {
    var faktor = 0.0
    var formelKodeEnum: FormelKodeEnum? = null
    var arsbelop = 0.0
    var justertbelop: Justertbelop? = null
    var tillegg = 0.0
}
