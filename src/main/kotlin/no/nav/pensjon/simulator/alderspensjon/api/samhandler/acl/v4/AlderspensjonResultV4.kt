package no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v4

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.pensjon.simulator.alderspensjon.PensjonSimuleringStatusKode
import no.nav.pensjon.simulator.alderspensjon.PensjonType
import java.time.LocalDate

/**
 * Version 4 of the data transfer object representing a result of the 'simuler alderspensjon' service.
 * Ref. API specification: https://confluence.adeo.no/pages/viewpage.action?pageId=583317319
 */
data class AlderspensjonResultV4(
    val simuleringSuksess: Boolean,
    val aarsakListeIkkeSuksess: List<PensjonSimuleringStatusV4>,
    val alderspensjon: List<AlderspensjonFraFolketrygdenV4>,
    val forslagVedForLavOpptjening: ForslagVedForLavOpptjeningV4?,
    val harUttak: Boolean
)

data class AlderspensjonFraFolketrygdenV4(
    @param:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val fraOgMedDato: LocalDate,
    val delytelseListe: List<PensjonDelytelseV4>,
    val uttaksgrad: Int
)

data class ForslagVedForLavOpptjeningV4(
    val gradertUttak: GradertUttakV4?,
    @param:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val heltUttakFraOgMedDato: LocalDate
)

data class GradertUttakV4(
    @param:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val fraOgMedDato: LocalDate,
    val uttaksgrad: Int
)

data class PensjonSimuleringStatusV4(
    val statusKode: String,
    val statusBeskrivelse: String
)

data class PensjonDelytelseV4(
    val pensjonsType: String,
    val belop: Int
)

enum class PensjonTypeV4(val externalValue: String, val internalValue: PensjonType) {
    INNTEKTSPENSJON("inntektsPensjon", PensjonType.INNTEKTSPENSJON),
    GARANTIPENSJON("garantiPensjon", PensjonType.GARANTIPENSJON);

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromInternalValue(value: PensjonType): PensjonTypeV4 = entries.single { it.internalValue == value }
    }
}

enum class PensjonSimuleringStatusKodeV4 (val externalValue: String, val internalValue: PensjonSimuleringStatusKode) {
    AVSLAG_FOR_LAV_OPPTJENING("AVSLAG_FOR_LAV_OPPTJENING", PensjonSimuleringStatusKode.AVSLAG_FOR_LAV_OPPTJENING),
    AVSLAG_FOR_KORT_TRYGDETID("AVSLAG_FOR_KORT_TRYGDETID", PensjonSimuleringStatusKode.AVSLAG_FOR_KORT_TRYGDETID),
    BRUKER_FOEDT_FOER_1943("BRUKER_FODT_FOR_1943", PensjonSimuleringStatusKode.BRUKER_FOEDT_FOER_1943),
    BRUKER_HAR_IKKE_LOEPENDE_ALDERSPENSJON("BRUKER_HAR_IKKE_LOPENDE_ALDERSPENSJON", PensjonSimuleringStatusKode.BRUKER_HAR_IKKE_LOEPENDE_ALDERSPENSJON),
    BRUKER_HAR_LOEPENDE_ALDERSPENSJON_PAA_GAMMELT_REGELVERK("BRUKER_HAR_LOPENDE_ALDERSPPENSJON_PAA_GAMMELT_REGELVERK", PensjonSimuleringStatusKode.BRUKER_HAR_LOEPENDE_ALDERSPENSJON_PAA_GAMMELT_REGELVERK),
    UGYLDIG_ENDRING_AV_UTTAKSGRAD("UGYLDIG_ENDRING_AV_UTTAKSGRAD", PensjonSimuleringStatusKode.UGYLDIG_ENDRING_AV_UTTAKSGRAD),
    ANNET("ANNET", PensjonSimuleringStatusKode.ANNET);

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromInternalValue(value: PensjonSimuleringStatusKode): PensjonSimuleringStatusKodeV4 =
            entries.singleOrNull { it.internalValue == value } ?: ANNET
    }
}
