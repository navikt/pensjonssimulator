package no.nav.pensjon.simulator.core.krav

enum class KravGjelder {
    /**
     * AFP etteroppgjør
     */
    AFP_EO,

    /**
     * Anke
     */
    ANKE,

    /**
     * Eksport
     */
    EKSPORT,

    /**
     * Endring uttaksgrad
     */
    ENDR_UTTAKSGRAD,

    /**
     * Erstatning
     */
    ERSTATNING,

    /**
     * Ettergivelse av gjeld
     */
    ETTERGIV_GJELD,

    /**
     * Dekning faste utgifter inst.opphold
     */
    FAS_UTG_IO,

    /**
     * Førstegangsbehandling
     */
    FORSTEG_BH,

    /**
     * Førstegangsbehandling bosatt utland
     */
    F_BH_BO_UTL,

    /**
     * Førstegangsbehandling kun utland
     */
    F_BH_KUN_UTL,

    /**
     * Førstegangsbehandling Norge/utland
     */
    F_BH_MED_UTL,

    /**
     * Gjenlevenderettighet
     */
    GJ_RETT,

    /**
     * Godskriving omsorgsopptjening
     */
    GOD_OMSGSP,

    /**
     * G-omregning
     */
    GOMR,

    /**
     * Hjelpeberegning ved overgang til uføretrygd
     */
    HJLPBER_OVERG_UT,

    /**
     * Inntektsendring
     */
    INNT_E,

    /**
     * Inntektskontroll
     */
    INNT_KTRL,

    /**
     * Klage
     */
    KLAGE,

    /**
     * Kontroll 3-17 a
     */
    KONTROLL_3_17_A,

    /**
     * Konvertert krav
     */
    KONVERTERING,

    /**
     * Minimalt konvertert krav
     */
    KONVERTERING_MIN,

    /**
     * Konvertering - Avvik ved G-omr
     */
    KONV_AVVIK_G_BATCH,

    /**
     * Mellombehandling
     */
    MELLOMBH,

    /**
     * Merskatt tilbakekreving
     */
    MTK,

    /**
     * Omgjøring av tilbakekreving
     */
    OMGJ_TILBAKE,

    /**
     * Overføring omsorgsopptjening
     */
    OVERF_OMSGSP,

    /**
     * Regulering
     */
    REGULERING,

    /**
     * Revurdering
     */
    REVURD,

    /**
     * Saksomkostninger
     */
    SAK_OMKOST,

    /**
     * Sluttbehandling Norge/utland
     */
    SLUTT_BH_UTL,

    /**
     * Søknad om økning av uføregrad
     */
    SOK_OKN_UG,

    /**
     * Søknad om reduksjon av uføregrad
     */
    SOK_RED_UG,

    /**
     * Søknad om ung ufør
     */
    SOK_UU,

    /**
     * Søknad om yrkesskade
     */
    SOK_YS,

    /**
     * Tilbakekreving
     */
    TILBAKEKR,

    /**
     * Etteroppgjør uføretrygd
     */
    UT_EO,

    /**
     * Vurdering av etteroppgjør
     */
    UT_VURDERING_EO,

    /**
     * Utsendelse til avtaleland
     */
    UTSEND_AVTALELAND,

    /**
     * Sluttbehandling kun utland
     */
    SLUTTBEH_KUN_UTL
}
