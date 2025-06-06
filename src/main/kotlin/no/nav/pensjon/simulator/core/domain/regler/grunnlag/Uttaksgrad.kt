package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.util.DateNoonExtension.noon
import java.util.*

// 2025-06-06, plus finishInit, minus compareTo
class Uttaksgrad {
    var fomDato: Date? = null
    var tomDato: Date? = null
    var uttaksgrad = 0

    /**
     * (Ref. PEN: CommonToReglerMapper.mapUttaksgradToRegler)
     */
    fun setDatesToNoon() {
        fomDato = fomDato?.noon()
        tomDato = fomDato?.noon()
    }
}
