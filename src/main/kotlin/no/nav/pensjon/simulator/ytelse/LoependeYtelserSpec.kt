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
    val pre2025OffentligAfpYtelserFlags: Pre2025OffentligAfpYtelserFlags?
)

data class AlderspensjonYtelserFlags(
    val inkluderPrivatAfp: Boolean
)

data class EndringAlderspensjonYtelserFlags(
    val inkluderPrivatAfp: Boolean
)

data class Pre2025OffentligAfpYtelserFlags(
    val gjelderFpp: Boolean,
    val sivilstatusUdefinert: Boolean
)
