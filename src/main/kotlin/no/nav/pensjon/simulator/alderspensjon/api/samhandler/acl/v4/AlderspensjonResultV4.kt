package no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v4

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.pensjon.simulator.alderspensjon.PensjonSimuleringStatusKode
import no.nav.pensjon.simulator.alderspensjon.PensjonType
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException
import no.nav.pensjon.simulator.validity.IngressErrorHandler.extractSafeMessage
import no.nav.pensjon.simulator.validity.Problem
import no.nav.pensjon.simulator.validity.ProblemType
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
) {
    companion object {

        fun forFeil(e: Exception, callId: String) =
            forFeil(
                beskrivelse = extractSafeMessage(e),
                status = PensjonSimuleringStatusKodeV4.ANNET,
                callId
            )

        fun forFeil(problem: Problem, callId: String) =
            forFeil(
                beskrivelse = problem.beskrivelse,
                status = problem.type.let(::simuleringStatusKode),
                callId
            )

        private fun forFeil(
            beskrivelse: String,
            status: PensjonSimuleringStatusKodeV4,
            callId: String
        ) =
            AlderspensjonResultV4(
                simuleringSuksess = false,
                aarsakListeIkkeSuksess = listOf(
                    PensjonSimuleringStatusV4(
                        statusKode = status.externalValue,
                        statusBeskrivelse = "Call ID: $callId | Error: feil ved simulering av alderspensjon V4 | Details: $beskrivelse"
                    )
                ),
                alderspensjon = emptyList(),
                forslagVedForLavOpptjening = null,
                harUttak = false
            )

        private fun simuleringStatusKode(type: ProblemType): PensjonSimuleringStatusKodeV4 =
            when (type) {
                ProblemType.UTILSTREKKELIG_OPPTJENING -> PensjonSimuleringStatusKodeV4.AVSLAG_FOR_LAV_OPPTJENING
                ProblemType.UTILSTREKKELIG_TRYGDETID -> PensjonSimuleringStatusKodeV4.AVSLAG_FOR_KORT_TRYGDETID
                else -> PensjonSimuleringStatusKodeV4.ANNET
            }
    }
}

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
        private val valuesByInternal = entries.associateBy { it.internalValue }

        fun fromInternalValue(value: PensjonType): PensjonTypeV4 =
            valuesByInternal[value]
                ?: throw InvalidEnumValueException("Mangler V4-mapping for intern pensjonstype $value")
    }
}

enum class PensjonSimuleringStatusKodeV4(val externalValue: String, val internalValue: PensjonSimuleringStatusKode) {
    AVSLAG_FOR_LAV_OPPTJENING("AVSLAG_FOR_LAV_OPPTJENING", PensjonSimuleringStatusKode.AVSLAG_FOR_LAV_OPPTJENING),
    AVSLAG_FOR_KORT_TRYGDETID("AVSLAG_FOR_KORT_TRYGDETID", PensjonSimuleringStatusKode.AVSLAG_FOR_KORT_TRYGDETID),
    BRUKER_FOEDT_FOER_1943("BRUKER_FODT_FOR_1943", PensjonSimuleringStatusKode.BRUKER_FOEDT_FOER_1943),
    BRUKER_HAR_IKKE_LOEPENDE_ALDERSPENSJON("BRUKER_HAR_IKKE_LOPENDE_ALDERSPENSJON", PensjonSimuleringStatusKode.BRUKER_HAR_IKKE_LOEPENDE_ALDERSPENSJON),
    BRUKER_HAR_LOEPENDE_ALDERSPENSJON_PAA_GAMMELT_REGELVERK("BRUKER_HAR_LOPENDE_ALDERSPPENSJON_PAA_GAMMELT_REGELVERK", PensjonSimuleringStatusKode.BRUKER_HAR_LOEPENDE_ALDERSPENSJON_PAA_GAMMELT_REGELVERK),
    UGYLDIG_ENDRING_AV_UTTAKSGRAD("UGYLDIG_ENDRING_AV_UTTAKSGRAD", PensjonSimuleringStatusKode.UGYLDIG_ENDRING_AV_UTTAKSGRAD),
    ANNET("ANNET", PensjonSimuleringStatusKode.ANNET);

    companion object {
        private val valuesByInternal = entries.associateBy { it.internalValue }

        fun fromInternalValue(value: PensjonSimuleringStatusKode): PensjonSimuleringStatusKodeV4 =
            valuesByInternal[value] ?: ANNET
    }
}
