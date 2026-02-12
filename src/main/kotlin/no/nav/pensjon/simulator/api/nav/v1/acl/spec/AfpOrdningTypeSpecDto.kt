package no.nav.pensjon.simulator.api.nav.v1.acl.spec

import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum

enum class AfpOrdningTypeSpecDto(val internalValue: AFPtypeEnum) {
    KOMMUNAL(internalValue = AFPtypeEnum.AFPKOM),
    STATLIG(internalValue = AFPtypeEnum.AFPSTAT),
    FINANSNAERINGEN(internalValue = AFPtypeEnum.FINANS),
    KONVERTERT_PRIVAT(internalValue = AFPtypeEnum.KONV_K),
    KONVERTERT_OFFENTLIG(internalValue = AFPtypeEnum.KONV_O),
    LO_NHO_ORDNINGEN(internalValue = AFPtypeEnum.LONHO),
    SPEKTER(internalValue = AFPtypeEnum.NAVO),
}