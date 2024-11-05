package no.nav.pensjon.simulator.ytelse

import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

data class LoependeYtelserSpec(
    val pid: Pid,
    val foersteUttakDato: LocalDate,
    val inkluderPrivatAfp: Boolean,
    val avdoedPid: Pid?,
    val doedDato: LocalDate?
)
