package no.nav.pensjon.simulator.generelt

import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum

data class Person(
    val statsborgerskap: LandkodeEnum
)
