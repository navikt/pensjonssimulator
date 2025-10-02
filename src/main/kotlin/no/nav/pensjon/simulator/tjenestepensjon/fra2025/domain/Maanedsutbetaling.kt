package no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain

import no.nav.pensjon.simulator.alder.Alder
import java.time.LocalDate

data class Maanedsutbetaling(
    val fraOgMedDato: LocalDate,
    val fraOgMedAlder: Alder,
    var maanedsBeloep: Int,
)
