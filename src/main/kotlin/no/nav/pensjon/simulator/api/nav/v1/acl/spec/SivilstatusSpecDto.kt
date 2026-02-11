package no.nav.pensjon.simulator.api.nav.v1.acl.spec

import no.nav.pensjon.simulator.core.domain.SivilstatusType

enum class SivilstatusSpecDto(val internalValue: SivilstatusType) {
    ENKE_ELLER_ENKEMANN(internalValue = SivilstatusType.ENKE),
    GIFT(internalValue = SivilstatusType.GIFT),
    GIFT_LEVER_ADSKILT(internalValue = SivilstatusType.GLAD),
    GJENLEVENDE_ETTER_SAMLIVSBRUDD(internalValue = SivilstatusType.GJES),
    GJENLEVENDE_PARTNER(internalValue = SivilstatusType.GJPA),
    GJENLEVENDE_SAMBOER(internalValue = SivilstatusType.GJSA),
    REGISTRERT_PARTNER(internalValue = SivilstatusType.REPA),
    REGISTRERT_PARTNER_LEVER_ADSKILT(internalValue = SivilstatusType.PLAD),
    SAMBOER(internalValue = SivilstatusType.SAMB),
    SEPARERT(internalValue = SivilstatusType.SEPR),
    SEPARERT_PARTNER(internalValue = SivilstatusType.SEPA),
    SKILT(internalValue = SivilstatusType.SKIL),
    SKILT_PARTNER(internalValue = SivilstatusType.SKPA),
    UGIFT(internalValue = SivilstatusType.UGIF),
    UKJENT(internalValue = SivilstatusType.NULL)
}
