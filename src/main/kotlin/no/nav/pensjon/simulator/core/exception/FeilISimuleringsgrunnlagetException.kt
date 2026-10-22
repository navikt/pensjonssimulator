package no.nav.pensjon.simulator.core.exception

// PEN071FeilISimuleringsgrunnlagetException
class FeilISimuleringsgrunnlagetException : RuntimeException {
    constructor(cause: Throwable) : super(cause)
    constructor(message: String) : super(message)
}
