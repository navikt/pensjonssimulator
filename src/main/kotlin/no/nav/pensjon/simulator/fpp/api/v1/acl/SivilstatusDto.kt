package no.nav.pensjon.simulator.fpp.api.v1.acl

import no.nav.pensjon.simulator.core.domain.SivilstatusType

enum class SivilstatusDto(val internalValue: SivilstatusType) {
    ENKE(internalValue = SivilstatusType.ENKE),
    GIFT(internalValue = SivilstatusType.GIFT),
    GLAD(internalValue = SivilstatusType.GLAD),
    GJES(internalValue = SivilstatusType.GJES),
    GJPA(internalValue = SivilstatusType.GJPA),
    GJSA(internalValue = SivilstatusType.GJSA),
    REPA(internalValue = SivilstatusType.REPA),
    PLAD(internalValue = SivilstatusType.PLAD),
    SAMB(internalValue = SivilstatusType.SAMB),
    SEPR(internalValue = SivilstatusType.SEPR),
    SEPA(internalValue = SivilstatusType.SEPA),
    SKIL(internalValue = SivilstatusType.SKIL),
    SKPA(internalValue = SivilstatusType.SKPA),
    UGIF(internalValue = SivilstatusType.UGIF),
    NULL(internalValue = SivilstatusType.NULL)
}