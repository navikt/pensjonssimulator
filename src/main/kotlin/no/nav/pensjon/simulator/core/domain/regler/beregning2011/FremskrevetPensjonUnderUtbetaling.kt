package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum

// 2025-03-10
class FremskrevetPensjonUnderUtbetaling : Regulering {
    var pensjonUnderUtbetalingPerAr = 0.0
    override var gap = 0
    override var reguleringsfaktor = 0.0
    var formelKodeEnum: FormelKodeEnum = FormelKodeEnum.BPUx

    constructor() : super() {
        formelKodeEnum = FormelKodeEnum.BPUx
    }

    constructor(source: FremskrevetPensjonUnderUtbetaling) : super() {
        pensjonUnderUtbetalingPerAr = source.pensjonUnderUtbetalingPerAr
        gap = source.gap
        reguleringsfaktor = source.reguleringsfaktor
        formelKodeEnum = source.formelKodeEnum
    }
}
