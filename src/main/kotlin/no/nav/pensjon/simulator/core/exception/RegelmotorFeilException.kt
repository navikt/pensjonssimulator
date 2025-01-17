package no.nav.pensjon.simulator.core.exception

// PEN: PEN222BeregningstjenesteFeiletException
class RegelmotorFeilException : RuntimeException {
    constructor(e: Throwable) : super(e)
    constructor(message: String?) : super(message)
}
