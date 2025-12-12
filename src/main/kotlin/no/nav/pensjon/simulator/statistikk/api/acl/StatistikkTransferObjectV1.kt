package no.nav.pensjon.simulator.statistikk.api.acl

data class StatistikkTransferObjectV1(
    val statistikk: List<HendelseAntallV1>
)

data class HendelseAntallV1(
    val hendelse: SimuleringHendelseV1,
    val antall: Int
)

data class SimuleringHendelseV1(
    val organisasjonsnummer: String,
    val simuleringstype: SimuleringTypeEnumV1
)

enum class SimuleringTypeEnumV1 {
    /**
     * AFP
     */
    AFP,

    /**
     * "Pre-2025" AFP i offentlig sektor etterfulgt av alderspensjon
     */
    AFP_ETTERF_ALDER,

    /**
     * AFP - vedtak om fremtidig pensjonspoeng
     */
    AFP_FPP,

    /**
     * Alderspensjon
     */
    ALDER,

    /**
     * Alderspensjon
     */
    ALDER_KAP_20,

    /**
     * Alderspensjon med AFP i privat sektor
     */
    ALDER_M_AFP_PRIVAT,

    /**
     * Alderspensjon med livsvarig AFP i offentlig sektor
     */
    ALDER_MED_AFP_OFFENTLIG_LIVSVARIG, // SIMDOM-ADD

    /**
     * Alderspensjon med gjenlevenderettigheter
     */
    ALDER_M_GJEN,

    /**
     * Barnepensjon
     */
    BARN,

    /**
     * Endring av alderspensjon
     */
    ENDR_ALDER,

    /**
     * Endring av alderspensjon med gjenlevenderettigheter
     */
    ENDR_ALDER_M_GJEN,

    /**
     * Endring av alderspensjon med AFP i privat sektor
     */
    ENDR_AP_M_AFP_PRIVAT,

    /**
     * Endring av alderspensjon med livsvarig AFP i offentlig sektor
     */
    ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG, // SIMDOM-ADD

    /**
     * Gjenlevendepensjon
     */
    GJENLEVENDE
}
