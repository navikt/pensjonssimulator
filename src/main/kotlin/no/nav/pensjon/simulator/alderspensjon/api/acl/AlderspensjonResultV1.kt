package no.nav.pensjon.simulator.alderspensjon.api.acl

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class AlderspensjonResultV1(
    val simuleringSuksess: Boolean,
    val aarsakListeIkkeSuksess: List<PensjonSimuleringStatusV1>,
    val alderspensjon: List<AlderspensjonFraFolketrygdenV1>,
    val alternativerVedForLavOpptjening: List<PensjonAlternativerVedForLavOpptjeningV1>,
    val harUttak: Boolean
)

data class AlderspensjonFraFolketrygdenV1(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val fraOgMedDato: LocalDate,
    val delytelseListe: List<PensjonDelytelseV1>,
    val uttaksgrad: Int
)

data class PensjonAlternativerVedForLavOpptjeningV1(
    val alderspensjonVedTidligstMuligUttak: AlderspensjonFraFolketrygdenV1,
    val alderspensjonVedHoyestMuligGrad: AlderspensjonFraFolketrygdenV1
)

data class PensjonSimuleringStatusV1(
    val statusKode: PensjonSimuleringStatusKodeV1,
    val statusBeskrivelse: String
)

data class PensjonDelytelseV1(
    val pensjonsType: PensjonTypeV1,
    val belop: Int
)

enum class PensjonTypeV1 {
    inntektsPensjon,
    garantiPensjon
}

enum class PensjonSimuleringStatusKodeV1 {
    AVSLAG_FOR_LAV_OPPTJENING,
    AVSLAG_FOR_KORT_TRYGDETID,
    BRUKER_FODT_FOR_1943,
    BRUKER_HAR_IKKE_LOPENDE_ALDERSPENSJON,
    BRUKER_HAR_LOPENDE_ALDERSPPENSJON_PAA_GAMMELT_REGELVERK,
    UGYLDIG_ENDRING_AV_UTTAKSGRAD,
    ANNET
}
