package no.nav.pensjon.simulator.core.out

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import java.time.LocalDate

data class OutputPensjonBeholdningPeriode(
    val pensjonBeholdning: Double,
    val garantipensjonBeholdning: Double,
    val garantitilleggBeholdning: Double,
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val datoFom: LocalDate,
    var garantipensjonNivaa: OutputGarantipensjonNivaa
)
