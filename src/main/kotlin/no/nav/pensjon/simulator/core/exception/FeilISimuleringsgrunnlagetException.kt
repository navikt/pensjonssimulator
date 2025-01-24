package no.nav.pensjon.simulator.core.exception

// PEN071FeilISimuleringsgrunnlagetException
// Can be replaced by KanIkkeBeregnesException?
class FeilISimuleringsgrunnlagetException : RuntimeException {
    constructor(cause: Throwable) : super(cause)
    constructor(message: String) : super(message)
}
