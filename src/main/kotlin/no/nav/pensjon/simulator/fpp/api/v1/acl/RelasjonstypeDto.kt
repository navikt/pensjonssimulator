package no.nav.pensjon.simulator.fpp.api.v1.acl

import no.nav.pensjon.simulator.fpp.RelasjonTypeCode

// PEN: no.nav.domain.pensjon.common.person.enums.RelasjonTypeCode
/**
 * Anti-corruption.
 * Versjonert utgave av relasjonstyper for bruk i API-et (isolerer API-et fra den interne representasjonen i domenet).
 */
enum class RelasjonstypeDto(val internalValue: RelasjonTypeCode) {
    BARN(internalValue = RelasjonTypeCode.BARN),
    EKTE(internalValue = RelasjonTypeCode.EKTE),
    ENKE(internalValue = RelasjonTypeCode.ENKE),
    FARA(internalValue = RelasjonTypeCode.FARA),
    FOBA(internalValue = RelasjonTypeCode.FOBA),
    FOFA(internalValue = RelasjonTypeCode.FOFA),
    FOMO(internalValue = RelasjonTypeCode.FOMO),
    GJPA(internalValue = RelasjonTypeCode.GJPA),
    GLAD(internalValue = RelasjonTypeCode.GLAD),
    MORA(internalValue = RelasjonTypeCode.MORA),
    REPA(internalValue = RelasjonTypeCode.REPA),
    SAMB(internalValue = RelasjonTypeCode.SAMB),
    SEPA(internalValue = RelasjonTypeCode.SEPA),
    SEPR(internalValue = RelasjonTypeCode.SEPR),
    SKIL(internalValue = RelasjonTypeCode.SKIL),
    SKPA(internalValue = RelasjonTypeCode.SKPA),
    SOSKEN(internalValue = RelasjonTypeCode.SOSKEN),
    MEDMOR(internalValue = RelasjonTypeCode.MEDMOR)
}