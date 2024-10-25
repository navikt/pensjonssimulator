package no.nav.pensjon.simulator.alderspensjon.api.tpo.direct.acl.v4

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.PensjonSimuleringStatusKode
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.PensjonType
import java.time.LocalDate

/**
 * Version 4 of result for 'simuler alderspensjon'.
 * Ref. API specification: https://confluence.adeo.no/pages/viewpage.action?pageId=583317319
 * NB: Versions 1 to 3 are services offered by PEN.
 */
data class AlderspensjonResultV4(
    val simuleringSuksess: Boolean,
    val aarsakListeIkkeSuksess: List<PensjonSimuleringStatusV4>,
    val alderspensjon: List<AlderspensjonFraFolketrygdenV4>,
    val forslagVedForLavOpptjening: ForslagVedForLavOpptjeningV4?,
    val harUttak: Boolean
)

data class AlderspensjonFraFolketrygdenV4(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val fraOgMedDato: LocalDate,
    val delytelseListe: List<PensjonDelytelseV4>,
    val uttaksgrad: Int
)

data class ForslagVedForLavOpptjeningV4(
    val gradertUttak: GradertUttakV4?,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val heltUttakFraOgMedDato: LocalDate
)

data class GradertUttakV4(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val fraOgMedDato: LocalDate,
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
        private val values = PensjonTypeV4.entries.toTypedArray()

        fun fromInternalValue(value: PensjonType): PensjonTypeV4 = values.single { it.internalValue == value }
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
        private val values = PensjonSimuleringStatusKodeV4.entries.toTypedArray()

        fun fromInternalValue(value: PensjonSimuleringStatusKode): PensjonSimuleringStatusKodeV4 =
            values.singleOrNull { it.internalValue == value } ?: ANNET
    }
}
