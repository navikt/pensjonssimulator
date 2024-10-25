package no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen

import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException
import java.time.LocalDate

data class AlderspensjonResult(
    val simuleringSuksess: Boolean,
    val aarsakListeIkkeSuksess: List<PensjonSimuleringStatus>,
    val alderspensjon: List<AlderspensjonFraFolketrygden>,
    val forslagVedForLavOpptjening: ForslagVedForLavOpptjening?,
    val harUttak: Boolean
)

data class AlderspensjonFraFolketrygden(
    val fom: LocalDate,
    val delytelseListe: List<PensjonDelytelse>,
    val uttaksgrad: Uttaksgrad
)

data class ForslagVedForLavOpptjening(
    val gradertUttak: GradertUttak?,
    val heltUttakFom: LocalDate
)

data class GradertUttak(
    val fom: LocalDate,
    val uttaksgrad: Uttaksgrad
)

data class PensjonSimuleringStatus(
    val statusKode: PensjonSimuleringStatusKode,
    val statusBeskrivelse: String
)

data class PensjonDelytelse(
    val pensjonType: PensjonType,
    val beloep: Int
)

enum class PensjonType {
    NONE,
    GARANTIPENSJON,
    INNTEKTSPENSJON
}

enum class PensjonSimuleringStatusKode {
    NONE,
    AVSLAG_FOR_LAV_OPPTJENING,
    AVSLAG_FOR_KORT_TRYGDETID,
    BRUKER_FOEDT_FOER_1943,
    BRUKER_HAR_IKKE_LOEPENDE_ALDERSPENSJON,
    BRUKER_HAR_LOEPENDE_ALDERSPENSJON_PAA_GAMMELT_REGELVERK,
    UGYLDIG_ENDRING_AV_UTTAKSGRAD,
    ANNET
}

enum class Uttaksgrad(val prosentsats: Int) {
    NULL(prosentsats = 0),
    TJUE_PROSENT(prosentsats = 20),
    FOERTI_PROSENT(prosentsats = 40),
    FEMTI_PROSENT(prosentsats = 50),
    SEKSTI_PROSENT(prosentsats = 60),
    AATTI_PROSENT(prosentsats = 80),
    HUNDRE_PROSENT(prosentsats = 100);

    companion object {
        private val values = entries.toTypedArray()

        fun from(prosentsats: Int?) =
            values.singleOrNull { it.prosentsats == prosentsats }
                ?: throw InvalidEnumValueException("Ugyldig prosentsats for uttaksgrad: $prosentsats")
    }
}
