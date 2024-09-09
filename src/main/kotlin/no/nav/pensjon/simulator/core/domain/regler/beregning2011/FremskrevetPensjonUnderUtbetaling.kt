package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti

class FremskrevetPensjonUnderUtbetaling : Regulering {
    var pensjonUnderUtbetalingPerAr: Double = 0.0
    override var gap: Int = 0
    override var reguleringsfaktor: Double = 0.0
    var formelKode: FormelKodeCti? = null

    constructor() : super() {
        formelKode = FormelKodeCti("BPUx")
    }

    constructor(fremskrevetPensjonUnderUtbetaling: FremskrevetPensjonUnderUtbetaling) : super() {
        pensjonUnderUtbetalingPerAr = fremskrevetPensjonUnderUtbetaling.pensjonUnderUtbetalingPerAr
        gap = fremskrevetPensjonUnderUtbetaling.gap
        reguleringsfaktor = fremskrevetPensjonUnderUtbetaling.reguleringsfaktor
        if (fremskrevetPensjonUnderUtbetaling.formelKode != null) {
            formelKode = FormelKodeCti(fremskrevetPensjonUnderUtbetaling.formelKode!!)
        }
    }

    constructor(
            pensjonUnderUtbetalingPerAr: Double = 0.0,
            gap: Int = 0,
            reguleringsfaktor: Double = 0.0,
            formelKode: FormelKodeCti? = null
    ) : this() {
        this.pensjonUnderUtbetalingPerAr = pensjonUnderUtbetalingPerAr
        this.gap = gap
        this.reguleringsfaktor = reguleringsfaktor
        this.formelKode = formelKode
    }
}
