package no.nav.pensjon.simulator.core.knekkpunkt

enum class KnekkpunktAarsak {
    /**
     * Ny opptjening avdød
     */
    OPPTJAVDOD,

    /**
     * Ny opptjening bruker
     */
    OPPTJBRUKER,

    /**
     * Ny trygdetid avdød
     */
    TTAVDOD,

    /**
     * Ny trygdetid bruker
     */
    TTBRUKER,

    /**
     * Endring av uttaksgrad
     */
    UTG
}
