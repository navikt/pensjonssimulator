package no.nav.pensjon.simulator.core.domain.regler.grunnlag

// 2025-06-06
class Barnekull {
    /**
     * Antall barn i kullet.
     */
    var antallBarn = 0

    /**
     * Angir om persondetaljen brukes som grunnlag på kravet.
     */
    var bruk: Boolean = true
}
