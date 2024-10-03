package no.nav.pensjon.simulator.core.domain.regler.enum

// TODO Ny, vi har ikke hatt noen Enum for dette, Pesys har OpptjeningPoppTypeCode
enum class OpptjeningPOPPTypeEnum {
    /**
     * Antatt inntekt
     */
    AI,
    /**
     * Diplomatinntekt - Jord/Skog/Fisk
     */
    DIP_JSF,
    /**
     * Diplomatinntekt - Lønn
     */
    DIP_LON,
    /**
     * Diplomatinntekt - Selvstendig
     */
    DIP_SEL,
    /**
     * Dagpengeopptjening
     */
    DP,
    /**
     * Dagpengeopptjening for fiskere og fangstmenn
     */
    DP_FF,
    /**
     * Innenlandsinntekt - Jord/Skog/Fisk
     */
    INN_JSF,
    /**
     * Innenlandsinntekt - Lønn
     */
    INN_LON,
    /**
     * Innenlandsinntekt - Selvstendig
     */
    INN_SEL,
    /**
     * Militær førstegangstjeneste
     */
    MIL,
    /**
     * Omsorg for barn over 6 år med hjelpestønad sats 3 eller 4
     */
    OBO6H,
    /**
     * Omsorg for barn over 7 år med hjelpestønad sats 3 eller 4
     */
    OBO7H,
    /**
     * Omsorg for barn under 6 år
     */
    OBU6,
    /**
     * Omsorg for barn under 7 år
     */
    OBU7,
    /**
     * Omsorg for syke/funksjonshemmede/eldre
     */
    OSFE,
    /**
     * PGI innland fastsatt av NAV
     */
    PGI_NAV,
    /**
     * Pensjonsgivende inntekt 1966 - Konv
     */
    PI66,
    /**
     * Pensjonspoeng for pensjonsgivende inntekt
     */
    PPI,
    /**
     * Reduksjonsinntekt
     */
    RED_INT,
    /**
     * Sivil førstegangstjeneste
     */
    SIV,
    /**
     * Sjøinntekt - Jord/Skog/Fisk
     */
    SJO_JSF,
    /**
     * Sjøinntekt - Lønn
     */
    SJO_LON,
    /**
     * Sjøinntekt - Selvstendig
     */
    SJO_SEL,
    /**
     * Sum pensjonsgivende inntekt
     */
    SUM_PI,
    /**
     * Svalbardinntekt - Jord/Skog/Fisk
     */
    SVA_JSF,
    /**
     * Svalbardinntekt - Lønn
     */
    SVA_LON,
    /**
     * Svalbardinntekt - Selvstendig
     */
    SVA_SEL,
    /**
     * Uføreopptjening
     */
    UFORE,
    /**
     * Utenlandsinntekt - Jord/Skog/Fisk
     */
    UTE_JSF,
    /**
     * Utenlandsinntekt - Lønn
     */
    UTE_LON,
    /**
     * Utenlandsinntekt - Selvstendig
     */
    UTE_SEL,
    /**
     * Fastland pensjonsgivendeinntekt av lønnsinntekt
     */
    FL_PGI_LOENN,
    /**
     * Fastland pensjonsgivendeinntekt av lønnsinntekt bare pensjonsdel
     */
    FL_PGI_LOENN_PD,
    /**
     * Fastland pensjonsgivendeinntekt av næringsinntekt
     */
    FL_PGI_NAERING,
    /**
     * Fastland pensjonsgivendeInntekt av næringsinntekt fra fiske, fangst eller familiebarnehage
     */
    FL_PGI_NAERING_FFF,
    /**
     * kildeskatt paa lønn pensjonsgivendeinntekt av lønnsinntekt
     */
    KSL_PGI_LOENN,
    /**
     * kildeskatt paa lønn pensjonsgivendeinntekt av lønnsinntekt bare pensjonsdel
     */
    KSL_PGI_LOENN_PD,
    /**
     * kildeskatt paa lønn pensjonsgivendeinntekt av næringsinntekt
     */
    KSL_PGI_NAERING,
    /**
     * kildeskatt paa lønn pensjonsgivendeInntekt av næringsinntekt fra fiske , fangst eller familiebarnehage
     */
    KSL_PGI_NAERING_FFF,
    /**
     * Svalbard pensjonsgivendeinntekt av lønnsinntekt
     */
    SVA_PGI_LOENN,
    /**
     * Svalbard pensjonsgivendeinntekt av lønnsinntekt bare pensjonsdel
     */
    SVA_PGI_LOENN_PD,
    /**
     * Svalbard pensjonsgivendeinntekt av næringsinntekt
     */
    SVA_PGI_NAERING,
    /**
     * Svalbard pensjonsgivendeInntekt av næringsinntekt fra fiske, fangst eller familiebarnehage
     */
    SVA_PGI_NAERING_FFF
}
