package no.nav.pensjon.simulator.uttak.api.acl

import mu.KotlinLogging
import no.nav.pensjon.simulator.validity.ProblemType
import org.springframework.http.HttpStatus

enum class TidligstMuligUttakFeilTypeV1(
    val internalValue: ProblemType,
    val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST
) {
    UGYLDIG_UTTAKSDATO(internalValue = ProblemType.UGYLDIG_UTTAKSDATO),
    UGYLDIG_UTTAKSGRAD(internalValue = ProblemType.UGYLDIG_UTTAKSGRAD),
    UGYLDIG_SIVILSTATUS(internalValue = ProblemType.UGYLDIG_SIVILSTATUS),
    UGYLDIG_INNTEKT(internalValue = ProblemType.UGYLDIG_INNTEKT),
    UGYLDIG_ANTALL_AAR(internalValue = ProblemType.UGYLDIG_ANTALL_AAR),
    UGYLDIG_PERSONIDENT(internalValue = ProblemType.UGYLDIG_PERSONIDENT),
    PERSON_IKKE_FUNNET(internalValue = ProblemType.PERSON_IKKE_FUNNET),
    PERSON_FOR_HOEY_ALDER(internalValue = ProblemType.PERSON_FOR_HOEY_ALDER),
    FOR_LAV_OPPTJENING(internalValue = ProblemType.UTILSTREKKELIG_OPPTJENING),
    FOR_LAV_TRYGDETID(internalValue = ProblemType.UTILSTREKKELIG_TRYGDETID),
    ANNEN_KLIENTFEIL(internalValue = ProblemType.ANNEN_KLIENTFEIL),
    TEKNISK_FEIL(internalValue = ProblemType.SERVERFEIL, httpStatus = HttpStatus.INTERNAL_SERVER_ERROR);

    companion object {
        private val logger = KotlinLogging.logger {}

        fun fromInternalValue(value: ProblemType) = entries.firstOrNull { it.internalValue == value }
            ?: TEKNISK_FEIL.also { logger.warn { "Ingen TMU-feiltype matcher problemtype $value" } }
    }
}
