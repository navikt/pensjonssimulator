package no.nav.pensjon.simulator.validity

class BadSpecException : RuntimeException {
    val problemType: ProblemType

    constructor(message: String) : this(message, ProblemType.ANNEN_KLIENTFEIL)

    constructor(message: String, problemType: ProblemType) : super(message) {
        this.problemType = problemType
    }
}