package no.nav.pensjon.simulator.person.relasjon

import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

/**
 * Representerer to personer som på en gitt dato har en relasjon.
 */
data class PersonPar(
    val pid1: Pid,
    val pid2: Pid,
    val dato: LocalDate
)
