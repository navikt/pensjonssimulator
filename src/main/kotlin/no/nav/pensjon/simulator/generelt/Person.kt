package no.nav.pensjon.simulator.generelt

import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import java.time.LocalDate

data class Person(
    val foedselDato: LocalDate,
    val statsborgerskap: LandkodeEnum
)
