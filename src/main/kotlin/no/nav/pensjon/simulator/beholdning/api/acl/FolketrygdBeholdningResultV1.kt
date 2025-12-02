package no.nav.pensjon.simulator.beholdning.api.acl

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import java.time.LocalDate

/**
 * Specified in internal Nav documentation:
 * confluence.adeo.no/pages/viewpage.action?pageId=583317319
 */
data class FolketrygdBeholdningResultV1(
    val pensjonsBeholdningsPeriodeListe: List<PensjonsbeholdningPeriodeV1>
)

data class PensjonsbeholdningPeriodeV1(
    val pensjonsBeholdning: Int,
    val garantiPensjonsBeholdning: Int,
    val garantitilleggsbeholdning: Int,
    val garantiPensjonsNiva: GarantipensjonNivaaV1,
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val fraOgMedDato: LocalDate
)

data class GarantipensjonNivaaV1(
    val belop: Int,
    val satsType: String,
    val sats: Int,
    val anvendtTrygdetid: Int
)
