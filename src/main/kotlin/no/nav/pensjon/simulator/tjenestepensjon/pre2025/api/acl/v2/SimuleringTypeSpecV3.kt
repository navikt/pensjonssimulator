package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2

import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum

//Kopi av NavSimuleringTypeSpecV2
enum class SimuleringTypeSpecV3(val externalValue: String, val internalValue: SimuleringTypeEnum) {
    /**
     * AFP
     */
    //AFP("AFP", SimuleringTypeEnum.AFP),

    /**
     * AFP i offentlig sektor etterfulgt av alderspensjon
     */
    AFP_ETTERF_ALDER("AFP_ETTERF_ALDER", SimuleringTypeEnum.AFP_ETTERF_ALDER),

    /**
     * AFP - vedtak om fremtidig pensjonspoeng
     */
    //AFP_FPP("AFP_FPP", SimuleringTypeEnum.AFP_FPP),

    /**
     * Alderspensjon
     */
    ALDER("ALDER", SimuleringTypeEnum.ALDER),

    /**
     * Alderspensjon
     */
    //ALDER_KAP_20("ALDER_KAP_20", SimuleringTypeEnum.ALDER_KAP_20),

    /**
     * Alderspensjon med AFP i privat sektor
     */
    ALDER_M_AFP_PRIVAT("ALDER_M_AFP_PRIVAT", SimuleringTypeEnum.ALDER_M_AFP_PRIVAT),

    /**
     * Alderspensjon med gjenlevenderettigheter
     */
    ALDER_M_GJEN("ALDER_M_GJEN", SimuleringTypeEnum.ALDER_M_GJEN),

    /**
     * Barnepensjon
     */
    //BARN("BARN", SimuleringTypeEnum.BARN),

    /**
     * Endring av alderspensjon
     */
    ENDR_ALDER("ENDR_ALDER", SimuleringTypeEnum.ENDR_ALDER),

    /**
     * Endring av alderspensjon med gjenlevenderettigheter
     */
    ENDR_ALDER_M_GJEN("ENDR_ALDER_M_GJEN", SimuleringTypeEnum.ENDR_ALDER_M_GJEN),

    /**
     * Endring av alderspensjon med AFP i privat sektor
     */
    ENDR_AP_M_AFP_PRIVAT("ENDR_AP_M_AFP_PRIVAT", SimuleringTypeEnum.ENDR_AP_M_AFP_PRIVAT);

    /**
     * Gjenlevendepensjon
     */
    //GJENLEVENDE("GJENLEVENDE", SimuleringTypeEnum.GJENLEVENDE);
}
