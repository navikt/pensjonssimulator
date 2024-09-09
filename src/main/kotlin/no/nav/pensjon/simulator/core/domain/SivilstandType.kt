package no.nav.pensjon.simulator.core.domain

// no.nav.domain.pensjon.kjerne.kodetabeller.SivilstandTypeCode
enum class SivilstandType {
    /**
     * Enke/-mann
     */
    ENKE,

    /**
     * Gift
     */
    GIFT,

    /**
     * Gjenlevende partner
     */
    GJPA,

    /**
     * Uoppgitt
     */
    NULL,

    /**
     * Registrert partner
     */
    REPA,

    /**
     * Separert partner
     */
    SEPA,

    /**
     * Separert
     */
    SEPR,

    /**
     * Skilt
     */
    SKIL,

    /**
     * Skilt partner
     */
    SKPA,

    /**
     * Ugift
     */
    UGIF
}
