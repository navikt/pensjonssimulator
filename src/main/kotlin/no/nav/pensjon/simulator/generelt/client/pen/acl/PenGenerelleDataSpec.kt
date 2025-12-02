package no.nav.pensjon.simulator.generelt.client.pen.acl

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import java.time.LocalDate

/**
 * Corresponds to SimulatorGenerelleDataSpec in pensjon-pen
 */
data class PenGenerelleDataSpec(
    val pid: String?,
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val anonymFoedselDato: LocalDate?,
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val foersteVirkning: LocalDate?,
    val satsPeriode: PenPeriodeSpec?,
    val inkludering: PenDataInkluderingSpec?
)

data class PenPeriodeSpec(
    val fomAar: Int?,
    val tomAar: Int?
)

data class PenDataInkluderingSpec(
    val afpSatser: Boolean,
    val delingstall: Boolean,
    val forholdstall: Boolean
)
