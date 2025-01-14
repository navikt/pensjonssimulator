package no.nav.pensjon.simulator.tech.time

import java.time.LocalDate

fun interface Time {
    fun today(): LocalDate
}
