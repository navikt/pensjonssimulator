package no.nav.pensjon.simulator.core.domain

enum class VedtakResultat {
    /**
     * Avslag
     */
    AVSL,

    /**
     * Innvilget
     */
    INNV,

    /**
     * Opphør
     */
    OPPHOR,

    /**
     * Velg resultat
     */
    VELG,

    /**
     * Til manuell behandling
     */
    VETIKKE
}
