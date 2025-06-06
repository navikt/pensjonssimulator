package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import java.util.*

// 2025-06-06
class Arbeidsforholdsgrunnlag {
    /**
     * Fom dato for arbeidsforholdet.
     */
    var fomDato: Date? = null

    /**
     * Tom dato for arbeidsforholdet.
     */
    var tomDato: Date? = null

    /**
     * Stillingsandel i prosent.
     */
    var stillingsprosent = 0

    /**
     * Navn på arbeidsgiver.
     */
    var arbeidsgiver: String? = null

    /**
     * Arbeidsgivers juridiske organisasjonsnummer.
     */
    var orgNummer: String? = null
}
