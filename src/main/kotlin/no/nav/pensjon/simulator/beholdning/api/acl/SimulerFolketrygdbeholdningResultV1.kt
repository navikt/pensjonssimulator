package no.nav.pensjon.simulator.beholdning.api.acl

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class SimulerFolketrygdbeholdningResultV1(
    val pensjonsBeholdningsPeriodeListe: List<PensjonsbeholdningPeriodeV1>
)

data class PensjonsbeholdningPeriodeV1(
    val pensjonsBeholdning: Int,
    val garantiPensjonsBeholdning: Int,
    val garantiPensjonsNiva: GarantipensjonNivaaV1,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val fraOgMedDato: LocalDate
)

data class GarantipensjonNivaaV1(
    val belop: Int,
    val satsType: SatsTypeV1,
    val sats: Int,
    val anvendtTrygdetid: Int
)

enum class SatsTypeV1 {
    ORDINAER,
    HOY
}
