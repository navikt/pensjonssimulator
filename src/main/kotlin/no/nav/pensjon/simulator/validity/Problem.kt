package no.nav.pensjon.simulator.validity

data class Problem(
    val type: ProblemType,
    val beskrivelse: String
) {
    fun log(t: Throwable, message: () -> Any?) {
        type.alvorlighetsgrad.log(t, message)
    }
}