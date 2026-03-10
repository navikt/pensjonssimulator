package no.nav.pensjon.simulator.validity

/**
 * Indikerer inkonsistens i data hentet fra interne databaser i Nav.
 * Årsaken kan f.eks. være feilregistrering eller saksbehandlingsfeil.
 */
class InternDataInkonsistensException : RuntimeException {
    val problemType: ProblemType

    constructor(message: String) : this(message, ProblemType.INTERN_DATA_INKONSISTENS)

    constructor(message: String, problemType: ProblemType) : super(message) {
        this.problemType = problemType
    }
}