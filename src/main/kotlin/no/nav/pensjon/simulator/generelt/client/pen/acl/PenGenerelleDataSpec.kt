package no.nav.pensjon.simulator.generelt.client.pen.acl

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

/**
 * Corresponds to SimulatorGenerelleDataSpec in pensjon-pen
 */
data class PenGenerelleDataSpec(
    val pid: String?,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val anonymFoedselDato: LocalDate?,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val foersteVirkning: LocalDate?,
    val satsPeriode: PenPeriodeSpec?,
    val inkludering: PenDataInkluderingSpec?
)

data class PenPeriodeSpec(
    val fomAar: Int?,
    val tomAar: Int?,
)

data class PenDataInkluderingSpec(
    val afpSatser: Boolean,
    val delingstall: Boolean,
    val forholdstall: Boolean
)
