package no.nav.pensjon.simulator.core.exception

// PEN: ImplementationUnrecoverableException
class ImplementationUnrecoverableException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
}
