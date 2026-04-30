package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import java.time.LocalDate

// 2026-04-23
class Arbeidsforholdsgrunnlag {
    /**
     * Fom dato for arbeidsforholdet.
     */
    var fomDatoLd: LocalDate? = null

    /**
     * Tom dato for arbeidsforholdet.
     */
    var tomDatoLd: LocalDate? = null

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
