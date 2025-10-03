package no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception

class TomSimuleringFraTpOrdningException(val tpOrdning: String) : RuntimeException() {
    override val message: String
        get() = "tom liste eller manglende simulering fra $tpOrdning"
}