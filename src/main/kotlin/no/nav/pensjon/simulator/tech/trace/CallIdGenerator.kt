package no.nav.pensjon.simulator.tech.trace

fun interface CallIdGenerator {
    fun newId(): String
}
