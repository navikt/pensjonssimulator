package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3

import jakarta.validation.constraints.NotNull
import no.nav.pensjon.simulator.validity.ProblemType
import org.springframework.http.HttpStatus

data class SimulerOffentligTjenestepensjonResultV3(
    val simulertPensjonListe: List<SimulertPensjonResultV3>? = null,
    val feilkode: FeilkodeV3? = null,
    val relevanteTpOrdninger: List<String>? = emptyList(),
    val problem: Pre2025TpV3Problem? = null
)

data class Pre2025TpV3Problem(
    @field:NotNull val kode: Pre2025TpV3ProblemType,
    @field:NotNull val beskrivelse: String
)

enum class Pre2025TpV3ProblemType(
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
    UTILSTREKKELIG_OPPTJENING(internalValue = ProblemType.UTILSTREKKELIG_OPPTJENING),
    UTILSTREKKELIG_TRYGDETID(internalValue = ProblemType.UTILSTREKKELIG_TRYGDETID),
    ANNEN_KLIENTFEIL(internalValue = ProblemType.ANNEN_KLIENTFEIL),
    INTERN_DATA_INKONSISTENS(
        internalValue = ProblemType.INTERN_DATA_INKONSISTENS,
        httpStatus = HttpStatus.INTERNAL_SERVER_ERROR
    ),
    IMPLEMENTASJONSFEIL(internalValue = ProblemType.IMPLEMENTASJONSFEIL, httpStatus = HttpStatus.INTERNAL_SERVER_ERROR),
    TREDJEPARTSFEIL(internalValue = ProblemType.TREDJEPARTSFEIL, httpStatus = HttpStatus.INTERNAL_SERVER_ERROR),
    ANNEN_SERVERFEIL(internalValue = ProblemType.ANNEN_SERVERFEIL, httpStatus = HttpStatus.INTERNAL_SERVER_ERROR);

    companion object {

        fun from(internalValue: ProblemType): Pre2025TpV3ProblemType =
            entries.firstOrNull { it.internalValue == internalValue } ?: ANNEN_SERVERFEIL
    }
}