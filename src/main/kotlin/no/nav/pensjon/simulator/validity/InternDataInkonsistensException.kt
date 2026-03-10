package no.nav.pensjon.simulator.validity

/**
 * Indikerer inkonsistens i data hentet fra interne databaser i Nav.
 * Årsaken kan f.eks. være feilregistrering eller saksbehandlingsfeil.
 */
class InternDataInkonsistensException : RuntimeException {

    constructor(message: String) : super(message)

    constructor(message: String, e: Throwable) : super(message, e)
}