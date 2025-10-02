package no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception

class TjenestepensjonSimuleringException(val msg: String? = null) : RuntimeException() {
    override val message: String
        get() = "Feil ved simulering av tjenestepensjon ${msg ?: ""}"
}