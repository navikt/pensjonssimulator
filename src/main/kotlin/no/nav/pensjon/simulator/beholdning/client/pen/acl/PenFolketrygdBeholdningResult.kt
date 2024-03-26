package no.nav.pensjon.simulator.beholdning.client.pen.acl

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class PenFolketrygdBeholdningResult(
    val pensjonBeholdningPeriodeListe: List<PenPensjonsbeholdningPeriode>? = null
)

data class PenPensjonsbeholdningPeriode(
    val pensjonBeholdning: Int? = null,
    val garantipensjonBeholdning: Int? = null,
    val garantipensjonNivaa: PenGarantipensjonNivaa? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val fom: LocalDate? = null
)

data class PenGarantipensjonNivaa(
    val beloep: Int? = null,
    val satsType: String? = null,
    val sats: Int? = null,
    val anvendtTrygdetid: Int? = null
)

