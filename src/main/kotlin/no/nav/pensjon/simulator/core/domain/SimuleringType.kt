package no.nav.pensjon.simulator.core.domain

enum class SimuleringType {
    /**
     * Alderspensjon
     */
    ALDER,

    /**
     * Alderspensjon med AFP livsvarig i privat sektor
     */
    ALDER_M_AFP_PRIVAT,

    /**
     * Alderspensjon med AFP livsvarig i offentlig sektor
     */
    ALDER_MED_AFP_OFFENTLIG_LIVSVARIG,

    /**
     * AFP i offentlig sektor etterfulgt av alderspensjon
     * NB: Gjelder "gammel" offentlig AFP (regelverk før 2025, unntatt apotekerne)
     */
    AFP_ETTERF_ALDER,

    /**
     * Alderspensjon med gjenlevenderettigheter
     * TODO: Check if relevant for 1963+
     */
    ALDER_M_GJEN,

    /**
     * Endring av alderspensjon
     */
    ENDR_ALDER,

    /**
     * Endring av alderspensjon med AFP i privat sektor
     */
    ENDR_AP_M_AFP_PRIVAT,

    /**
     * Endring av alderspensjon med gjenlevenderettigheter
     * TODO: Check if relevant for 1963+
     */
    ENDR_ALDER_M_GJEN
}
