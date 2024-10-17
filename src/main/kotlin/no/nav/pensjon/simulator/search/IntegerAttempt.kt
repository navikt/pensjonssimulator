package no.nav.pensjon.simulator.search

fun interface IntegerAttempt<T : ValueAssessment> {
    fun tryValue(value: Int): T
}
