package no.nav.pensjon.simulator.ytelse

import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

data class LoependeYtelserSpec(
    val pid: Pid?, // null if anonym
    val foersteUttakDato: LocalDate,
    val avdoed: Avdoed?,
    val alderspensjonFlags: AlderspensjonYtelserFlags?,
    val endringAlderspensjonFlags: EndringAlderspensjonYtelserFlags?,
    val tidsbegrensetOffentligAfpYtelserFlags: TidsbegrensetOffentligAfpYtelserFlags?
)

data class AlderspensjonYtelserFlags(
    val inkluderPrivatAfp: Boolean
)

data class EndringAlderspensjonYtelserFlags(
    val inkluderPrivatAfp: Boolean
)

data class TidsbegrensetOffentligAfpYtelserFlags(
    val gjelderFpp: Boolean,
    val sivilstatusUdefinert: Boolean
)