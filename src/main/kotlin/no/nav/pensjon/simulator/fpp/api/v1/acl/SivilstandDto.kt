package no.nav.pensjon.simulator.fpp.api.v1.acl

import no.nav.pensjon.simulator.core.domain.regler.enum.SivilstandEnum

enum class SivilstandDto(val internalValue: SivilstandEnum) {
    /**
     * Enke/-mann
     */
    ENKE(internalValue = SivilstandEnum.ENKE),

    /**
     * Gift
     */
    GIFT(internalValue = SivilstandEnum.GIFT),

    /**
     * Gjenlevende partner
     */
    GJPA(internalValue = SivilstandEnum.GJPA),

    /**
     * Uoppgitt
     */
    NULL(internalValue = SivilstandEnum.NULL),

    /**
     * Registrert partner
     */
    REPA(internalValue = SivilstandEnum.REPA),

    /**
     * Separert partner
     */
    SEPA(internalValue = SivilstandEnum.SEPA),

    /**
     * Separert
     */
    SEPR(internalValue = SivilstandEnum.SEPR),

    /**
     * Skilt
     */
    SKIL(internalValue = SivilstandEnum.SKIL),

    /**
     * Skilt partner
     */
    SKPA(internalValue = SivilstandEnum.SKPA),

    /**
     * Ugift
     */
    UGIF(internalValue = SivilstandEnum.UGIF)
}
