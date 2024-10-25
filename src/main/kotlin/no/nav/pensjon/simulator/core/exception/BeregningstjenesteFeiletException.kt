package no.nav.pensjon.simulator.core.exception

// PEN222BeregningstjenesteFeiletException
class BeregningstjenesteFeiletException : RuntimeException {
    constructor(e: Throwable) : super(e)
    constructor(message: String?) : super(message)
}
