package no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception

class TpOrdningStoettesIkkeException(val tpOrdning: String) : RuntimeException() {
    override val message: String
        get() = "$tpOrdning støtter ikke simulering av tjenestepensjon v2025"
}