package no.nav.pensjon.simulator.core.out

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

// no.nav.pensjon.pen.domain.api.simulering.SimulatorSimulertPensjonBeholdningPeriode
data class OutputPensjonBeholdningPeriode(
    val pensjonBeholdning: Double,
    val garantipensjonBeholdning: Double,
    val garantitilleggBeholdning: Double,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET")
    val datoFom: LocalDate,

    var garantipensjonNivaa: OutputGarantipensjonNivaa
)
