package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import java.util.*

// 2025-06-06
class ArbeidsforholdEtterUforgrunnlag {
    /**
     * Fom date for arbeidsforholdet.
     */
    var fomDato: Date? = null

    /**
     * Work load.
     */
    var stillingsprosent = 0

    /**
     * if it is lasting facilitated work
     */
    var varigTilrettelagtArbeid = false
}
