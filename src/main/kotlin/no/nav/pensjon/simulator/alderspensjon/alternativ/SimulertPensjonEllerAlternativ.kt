package no.nav.pensjon.simulator.alderspensjon.alternativ

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import java.time.LocalDate

data class SimulertPensjonEllerAlternativ(
    val pensjon: SimulertPensjon?,
    val alternativ: SimulertAlternativ?
)

// PEN: SimulatorSimulertAlternativ
data class SimulertAlternativ(
    val gradertUttakAlder: SimulertUttakAlder?,
    val uttakGrad: UttakGradKode,
    val heltUttakAlder: SimulertUttakAlder,
    val resultStatus: SimulatorResultStatus
)

// PEN: SimulatorSimulertUttaksalder
data class SimulertUttakAlder(
    val alder: Alder,
    val uttakDato: LocalDate
)

enum class SimulatorResultStatus {
    GOOD,
    SUBOPTIMAL,
    BAD
}
