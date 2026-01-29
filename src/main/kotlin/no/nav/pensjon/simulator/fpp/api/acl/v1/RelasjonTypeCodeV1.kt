package no.nav.pensjon.simulator.fpp.api.acl.v1

import no.nav.pensjon.simulator.fpp.RelasjonTypeCode

// PEN: no.nav.domain.pensjon.common.person.enums.RelasjonTypeCode
/**
 * Anti-corruption.
 * Versjonert utgave av relasjonstyper for bruk i API-et (isolerer API-et fra den interne representasjonen i domenet).
 */
enum class RelasjonTypeCodeV1(val internalValue: RelasjonTypeCode) {
    BARN(internalValue = RelasjonTypeCode.BARN),
    ENKE(internalValue = RelasjonTypeCode.ENKE),
    FARA(internalValue = RelasjonTypeCode.FARA),
    GJPA(internalValue = RelasjonTypeCode.GJPA),
    GLAD(internalValue = RelasjonTypeCode.GLAD),
    MORA(internalValue = RelasjonTypeCode.MORA),
    REPA(internalValue = RelasjonTypeCode.REPA),
    SAMB(internalValue = RelasjonTypeCode.SAMB),
    SEPA(internalValue = RelasjonTypeCode.SEPA),
    SEPR(internalValue = RelasjonTypeCode.SEPR),
    SKIL(internalValue = RelasjonTypeCode.SKIL),
    SKPA(internalValue = RelasjonTypeCode.SKPA)
}
