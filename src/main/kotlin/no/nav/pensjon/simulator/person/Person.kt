package no.nav.pensjon.simulator.person

import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.person.Sivilstandstype
import java.time.LocalDate

data class Person(
    val foedselsdato: LocalDate?,
    val sivilstand: Sivilstandstype?,
    val statsborgerskap: LandkodeEnum?
)