package no.nav.pensjon.simulator.alderspensjon.client.pen.acl

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

/**
 * Ref. API specification:
 * https://confluence.adeo.no/pages/viewpage.action?pageId=583317319
 */
data class PenAlderspensjonResult(
    val simuleringSuksess: Boolean?,
    val aarsakListeIkkeSuksess: List<PenPensjonSimuleringStatus>?,
    val alderspensjon: List<PenAlderspensjonFraFolketrygden>?,
    val alternativerVedForLavOpptjening: PenForslagVedForLavOpptjening?,
    val harUttak: Boolean?
)

data class PenAlderspensjonFraFolketrygden(
    val fraOgMedDato: String?,
    val delytelseListe: List<PenPensjonDelytelse>?,
    val uttaksgrad: Int?
)

data class PenForslagVedForLavOpptjening(
    val gradertUttak: PenGradertUttak?,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val heltUttakFraOgMedDato: LocalDate
)

data class PenGradertUttak(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val fraOgMedDato: LocalDate,
    val uttaksgrad: Int
)

data class PenPensjonSimuleringStatus(
    val statusKode: String?,
    val statusBeskrivelse: String?
)

data class PenPensjonDelytelse(
    val pensjonsType: String?,
    val belop: Int?
)

