package no.nav.pensjon.simulator.core.trygd

import no.nav.pensjon.simulator.core.domain.Land
import java.time.LocalDate

// no.nav.domain.pensjon.kjerne.simulering.UtenlandsperiodeForSimulering
data class UtlandPeriode(
    val fom: LocalDate,
    val tom: LocalDate?,
    val land: Land,
    val arbeidet: Boolean
)

