package no.nav.pensjon.simulator.tech.time

import java.time.LocalTime

fun interface Tid {
    fun naa(): LocalTime
}