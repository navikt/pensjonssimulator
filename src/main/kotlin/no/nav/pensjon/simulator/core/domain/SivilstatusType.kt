package no.nav.pensjon.simulator.core.domain

//TODO SivilstatusType vs SivilstandType
// This enum is 1:1 with no.nav.domain.pensjon.kjerne.kodetabeller.SivilstatusTypeCode
enum class SivilstatusType {
    /**
     * Enke/-mann
     */
    ENKE,

    /**
     * Gift
     */
    GIFT,

    /**
     * Gjenlevende etter samlivsbrudd
     */
    GJES,

    /**
     * Gjenlevende partner
     */
    GJPA,

    /**
     * Gjenlevende samboer
     */
    GJSA,

    /**
     * Gift, lever adskilt
     */
    GLAD,

    /**
     * -
     */
    NULL,

    /**
     * Registrert partner, lever adskilt
     */
    PLAD,

    /**
     * Registrert partner
     */
    REPA,

    /**
     * Samboer
     */
    SAMB,

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
