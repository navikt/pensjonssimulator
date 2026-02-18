package no.nav.pensjon.simulator.core.domain.regler.enum

/**
 * pensjon-regler-api: no/nav/pensjon/regler/domain/enum/SivilstandEnum.kt
 * Copied 2025-05-22
 * Plus added comments
 * Ref. lovdata.no/forskrift/2017-07-14-1201/§3-1-1
 */
enum class SivilstandEnum {
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
