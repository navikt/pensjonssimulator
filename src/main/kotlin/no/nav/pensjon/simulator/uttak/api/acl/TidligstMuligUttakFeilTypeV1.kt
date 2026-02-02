package no.nav.pensjon.simulator.uttak.api.acl

import no.nav.pensjon.simulator.validity.ProblemType
import org.springframework.http.HttpStatus

enum class TidligstMuligUttakFeilTypeV1(
    val internalValue: ProblemType,
    val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST
) {

    FOR_LAV_OPPTJENING(internalValue = ProblemType.UTILSTREKKELIG_OPPTJENING),
    TEKNISK_FEIL(internalValue = ProblemType.SERVERFEIL, httpStatus = HttpStatus.INTERNAL_SERVER_ERROR);

    companion object {
        fun fromInternalValue(value: ProblemType) = entries.first { it.internalValue == value }
    }
}
