package no.nav.pensjon.simulator.core.domain.regler.enum

/**
 * pensjon-regler-api: no/nav/pensjon/regler/domain/enum/GrunnlagsrolleEnum.kt
 * Copied 2025-08-26
 * Plus added comments
 */
enum class GrunnlagsrolleEnum {
    /**
     * Avdødes barn
     */
    ABARN,

    /**
     * Avdødes ektefelle
     */
    AEKTEF,

    /**
     * Avdødes fosterbarn
     */
    AFBARN,

    /**
     * Avdødes partner
     */
    APARTNER,

    /**
     * Avdødes samboer
     */
    ASAMBO,

    /**
     * Avdød
     */
    AVDOD,

    /**
     * Barn
     */
    BARN,

    /**
     * Ektefelle
     */
    EKTEF,

    /**
     * Far
     */
    FAR,

    /**
     * Fosterbarn
     */
    FBARN,

    /**
     * Mor
     */
    MOR,

    /**
     * Partner
     */
    PARTNER,

    /**
     * Samboer
     */
    SAMBO,

    /**
     * Bruker ("søker")
     */
    SOKER,

    /**
     * Søsken
     */
    SOSKEN

    // NB: PEN GrunnlagRolle also has MEDMOR
}
