package no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception

class BrukerErIkkeMedlemException : RuntimeException() {
    override val message: String
        get() = "Bruker er ikke medlem av en offentlig tjenestepensjonsordning"
}