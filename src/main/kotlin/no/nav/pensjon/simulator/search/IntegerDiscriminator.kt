package no.nav.pensjon.simulator.search

fun interface IntegerDiscriminator {
    fun valueIsGood(value: Int): Boolean
}
