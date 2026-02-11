package no.nav.pensjon.simulator.api.nav.v1.acl.spec

import no.nav.pensjon.simulator.core.krav.UttakGradKode

enum class UttaksgradSpecDto(val internalValue: UttakGradKode) {
    NULL(internalValue = UttakGradKode.P_0),
    TJUE_PROSENT(internalValue = UttakGradKode.P_20),
    FOERTI_PROSENT(internalValue = UttakGradKode.P_40),
    FEMTI_PROSENT(internalValue = UttakGradKode.P_50),
    SEKSTI_PROSENT(internalValue = UttakGradKode.P_60),
    AATTI_PROSENT(internalValue = UttakGradKode.P_80),
    HUNDRE_PROSENT(internalValue = UttakGradKode.P_100)
}
