package no.nav.pensjon.simulator.api.nav.v1.acl.spec

import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum

enum class SimuleringstypeSpecDto(val internalValue: SimuleringTypeEnum) {
    ALDERSPENSJON_MED_TIDSBEGRENSET_OFFENTLIG_AFP(internalValue = SimuleringTypeEnum.AFP_ETTERF_ALDER),
    ALDERSPENSJON(internalValue = SimuleringTypeEnum.ALDER),
    ALDERSPENSJON_MED_PRIVAT_AFP(internalValue = SimuleringTypeEnum.ALDER_M_AFP_PRIVAT),
    ALDERSPENSJON_MED_LIVSVARIG_OFFENTLIG_AFP(internalValue = SimuleringTypeEnum.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG),
    ALDERSPENSJON_MED_GJENLEVENDERETT(internalValue = SimuleringTypeEnum.ALDER_M_GJEN),
    ENDRING_ALDERSPENSJON(internalValue = SimuleringTypeEnum.ENDR_ALDER),
    ENDRING_ALDERSPENSJON_MED_PRIVAT_AFP(internalValue = SimuleringTypeEnum.ENDR_AP_M_AFP_PRIVAT),
    ENDRING_ALDERSPENSJON_MED_LIVSVARIG_OFFENTLIG_AFP(internalValue = SimuleringTypeEnum.ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG),
    ENDRING_ALDERSPENSJON_MED_GJENLEVENDERETT(internalValue = SimuleringTypeEnum.ENDR_ALDER_M_GJEN)
}
