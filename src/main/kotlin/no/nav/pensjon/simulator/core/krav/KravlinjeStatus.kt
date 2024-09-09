package no.nav.pensjon.simulator.core.krav

enum class KravlinjeStatus {
    /**
     * Attestert
     */
    ATT,

    /**
     * Beregnet
     */
    BEREGNET,

    /**
     * Feilregistrert
     */
    FEILREGISTRERT,

    /**
     * Ferdig Behandlet
     */
    FERDIG,

    /**
     * Henlagt
     */
    HENLAGT,

    /**
     * Klar til attestering
     */
    KLAR_TIL_ATT,

    /**
     * Til behandling
     */
    TIL_BEHANDLING,

    /**
     * Trukket
     */
    TRUKKET,

    /**
     * Venter på Fellesordningen
     */
    VENTER_AFP,

    /**
     * Venter på bruker
     */
    VENTER_BRUKER,

    /**
     * Venter på opplysninger
     */
    VENTER_OPPL,

    /**
     * Venter på trygderetten
     */
    VENTER_RETT,

    /**
     * Venter på saksbehandling
     */
    VENTER_SAKSBEH,

    /**
     * Sendt utland
     */
    VENTER_UTL,

    /**
     * Vilkårsprøvd
     */
    VILKARSPROVD,

    /**
     * Venter på behandling av BPEN093
     */
    PA_VENT
}
