package no.nav.pensjon.simulator.core.afp

//TODO replace by AFPtypeEnum
// no.nav.domain.pensjon.kjerne.kodetabeller.AfpOrdningTypeCode
enum class AfpOrdningType {
    /**
     * AFP - Kommunalsektor
     */
    AFPKOM,

    /**
     * AFP - Stat
     */
    AFPSTAT,

    /**
     * Finansnæringen
     */
    FINANS,

    /**
     * Konvertert privat
     */
    KONV_K,

    /**
     * Konvertert offentlig
     */
    KONV_O,

    /**
     * LO/NHO - ordningen
     */
    LONHO,

    /**
     * Spekter
     */
    NAVO
}
