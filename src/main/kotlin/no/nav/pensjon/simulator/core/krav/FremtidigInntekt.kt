package no.nav.pensjon.simulator.core.krav

import java.time.LocalDate

// no.nav.domain.pensjon.kjerne.simulering.FremtidigInntekt
data class FremtidigInntekt(
    val aarligInntektBeloep: Int,
    val fom: LocalDate
)
