package no.nav.pensjon.simulator.uttak.client.pen.acl

import no.nav.pensjon.simulator.uttak.UttakGrad

/**
 * The 'externalValue' is uttaksgrad values used by PEN (pensjonsfaglig kjerne).
 * The source of PEN's uttaksgrad values is:
 * https://github.com/navikt/pesys/blob/main/pen/domain/nav-domain-pensjon-pen-api/src/main/java/no/nav/domain/pensjon/kjerne/kodetabeller/UttaksgradCode.java
 */
enum class PenUttaksgrad(val externalValue: String, val internalValue: UttakGrad) {

    NULL("P_0", UttakGrad.NULL),
    TJUE_PROSENT("P_20", UttakGrad.TJUE_PROSENT),
    FOERTI_PROSENT("P_40", UttakGrad.FOERTI_PROSENT),
    FEMTI_PROSENT("P_50", UttakGrad.FEMTI_PROSENT),
    SEKSTI_PROSENT("P_60", UttakGrad.SEKSTI_PROSENT),
    AATTI_PROSENT("P_80", UttakGrad.AATTI_PROSENT),
    HUNDRE_PROSENT("P_100", UttakGrad.HUNDRE_PROSENT);

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromInternalValue(grad: UttakGrad?): PenUttaksgrad =
            entries.firstOrNull { it.internalValue == grad } ?: HUNDRE_PROSENT
    }
}
