package no.nav.pensjon.simulator.api.nav.v1.acl

import no.nav.pensjon.simulator.core.krav.UttakGradKode

enum class UttaksgradDto(val internalValue: UttakGradKode) {
    NULL(internalValue = UttakGradKode.P_0),
    TJUE_PROSENT(internalValue = UttakGradKode.P_20),
    FOERTI_PROSENT(internalValue = UttakGradKode.P_40),
    FEMTI_PROSENT(internalValue = UttakGradKode.P_50),
    SEKSTI_PROSENT(internalValue = UttakGradKode.P_60),
    AATTI_PROSENT(internalValue = UttakGradKode.P_80),
    HUNDRE_PROSENT(internalValue = UttakGradKode.P_100);

    companion object {
        fun fromInternalValue(value: UttakGradKode): UttaksgradDto =
            UttaksgradDto.entries.singleOrNull { it.internalValue == value }
                ?: throw IllegalArgumentException("Intern verdi ikke støttet i API Nav v1 - uttaksgrad $value")
    }
}