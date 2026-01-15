package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import java.time.LocalDate

// no.nav.domain.pensjon.kjerne.simulering.UtenlandsperiodeForSimulering
data class UtlandPeriode(
    val fom: LocalDate,
    val tom: LocalDate?,
    val land: LandkodeEnum,
    val arbeidet: Boolean
)
