package no.nav.pensjon.simulator.fpp.api.v1.acl

import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum

enum class AfpTypeDto(val internalValue: AFPtypeEnum) {
    // LO/NHO
    LONHO(internalValue = AFPtypeEnum.LONHO),

    // Spekter
    NAVO(internalValue = AFPtypeEnum.NAVO),

    // Finansnæringen
    FINANS(internalValue = AFPtypeEnum.FINANS),

    // AFP Stat
    AFPSTAT(internalValue = AFPtypeEnum.AFPSTAT),

    // AFP i kommunal sektor
    AFPKOM(internalValue = AFPtypeEnum.AFPKOM),

    // Konvertert offentlig
    KONV_O(internalValue = AFPtypeEnum.KONV_O),

    // Konvertert privat
    KONV_K(internalValue = AFPtypeEnum.KONV_K)
}