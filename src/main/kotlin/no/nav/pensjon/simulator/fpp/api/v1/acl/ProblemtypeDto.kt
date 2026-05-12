package no.nav.pensjon.simulator.fpp.api.v1.acl

import no.nav.pensjon.simulator.validity.ProblemType
import org.springframework.http.HttpStatus

enum class ProblemtypeDto(
    val internalValue: ProblemType,
    val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST
) {
    UGYLDIG_UTTAKSDATO(internalValue = ProblemType.UGYLDIG_UTTAKSDATO),
    UGYLDIG_UTTAKSGRAD(internalValue = ProblemType.UGYLDIG_UTTAKSGRAD),
    UGYLDIG_SIVILSTATUS(internalValue = ProblemType.UGYLDIG_SIVILSTATUS),
    UGYLDIG_INNTEKT(internalValue = ProblemType.UGYLDIG_INNTEKT),
    UGYLDIG_ANTALL_AAR(internalValue = ProblemType.UGYLDIG_ANTALL_AAR),
    UGYLDIG_PERSONIDENT(internalValue = ProblemType.UGYLDIG_PERSONIDENT),
    PERSON_IKKE_FUNNET(internalValue = ProblemType.PERSON_IKKE_FUNNET, httpStatus = HttpStatus.NOT_FOUND),
    PERSON_FOR_HOEY_ALDER(internalValue = ProblemType.PERSON_FOR_HOEY_ALDER),
    UTILSTREKKELIG_OPPTJENING(internalValue = ProblemType.UTILSTREKKELIG_OPPTJENING, httpStatus = HttpStatus.OK),
    UTILSTREKKELIG_TRYGDETID(internalValue = ProblemType.UTILSTREKKELIG_TRYGDETID, httpStatus = HttpStatus.OK),
    ANNEN_KLIENTFEIL(internalValue = ProblemType.ANNEN_KLIENTFEIL),
    IMPLEMENTASJONSFEIL(internalValue = ProblemType.IMPLEMENTASJONSFEIL, httpStatus = HttpStatus.INTERNAL_SERVER_ERROR),
    INTERN_DATAFEIL(internalValue = ProblemType.INTERN_DATA_INKONSISTENS, httpStatus = HttpStatus.INTERNAL_SERVER_ERROR),
    TREDJEPARTSFEIL(internalValue = ProblemType.TREDJEPARTSFEIL, httpStatus = HttpStatus.INTERNAL_SERVER_ERROR),
    ANNEN_SERVERFEIL(internalValue = ProblemType.ANNEN_SERVERFEIL, httpStatus = HttpStatus.INTERNAL_SERVER_ERROR)
}