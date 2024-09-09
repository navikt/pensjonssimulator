package no.nav.pensjon.simulator.core.beholdning

// no.nav.domain.pensjon.kjerne.kodetabeller.RestpensjonBeholdningOppdateringArsakCode
enum class RestpensjonBeholdningOppdateringAarsak {
    /**
     * Oppdatert på grunn av ny opptjening
     */
    NY_OPPTJENING,

    /**
     * Oppdatert på grunn av regulering
     */
    REGULERING,

    /**
     * Oppdatert på grunn av nytt vedtak
     */
    VEDTAK
}
