package no.nav.pensjon.simulator.core.beregn

// no.nav.domain.pensjon.kjerne.kodetabelle.BegrunnelseCode
enum class BegrunnelseType {
    /**
     * Er forsørget av ektefelle
     */
    ANNEN_HAR_ET,

    /**
     * Annulering
     */
    ANNULERING,

    /**
     * Brukerinitiert
     */
    BRUKERINITIERT,

    /**
     * Krav om AP er avslått
     */
    BRUKER_AVSLAG_AP,

    /**
     * Har gjenlevendeytelse
     */
    BRUKER_HAR_GJP,

    /**
     * Har ikke/søker ikke AP
     */
    BRUKER_IKKE_AP,

    /**
     * Har ikke 100% AP
     */
    BRUKER_IKKE_HEL_AP,

    /**
     * Har løpende UP
     */
    BRUKER_LOP_UP,

    /**
     * Bruker er under 67 år
     */
    BRUKER_UNDER_67M,

    /**
     * Har hatt UP etter 62 år
     */
    BRUKER_UP_E_62,

    /**
     * Barn forsørget av andre
     */
    BT_GITT_TIL_ANNEN,

    /**
     * Barnets inntekt>1G
     */
    BT_INNT_OVER_1G,

    /**
     * Har ikke 100% AP
     */
    BT_KREVER_100_INNV,

    /**
     * Bruker er under 67 år
     */
    BT_MINST_67,

    /**
     * Barn over 18 år
     */
    BT_OVER_18,

    /**
     * Dødsfall
     */
    DODSFALL,

    /**
     * Eksport
     */
    EKSPORT,

    /**
     * Ektefelle 100% AP rett
     */
    EPS_HAR_RETT_TIL_AP,

    /**
     * Tilstøt. har AP/UP/AFP
     */
    EPS_HAR_YTELSE,

    /**
     * Ektefelles inntekt>1G
     */
    EPS_INNT_OVER_1G,

    /**
     * Ektefelle er over 67 år
     */
    EPS_OVER67M_AP_RETT,

    /**
     * Ektefelle ikke under 67
     */
    EPS_OVER_67M,

    /**
     * Tilstøt.er §3-2 samboer
     */
    EPS_SAMBOER_3_2,

    /**
     * Tilstøt. under 60 år
     */
    EPS_UNDER_60_AFP,

    /**
     * Forsørget av andre
     */
    ET_GITT_TIL_ANNEN,

    /**
     * Ikke 100% AP/UP/AFP
     */
    ET_KREVER_100_AP,

    /**
     * Har familiepleierytelse
     */
    FAMPL_OG_AP,

    /**
     * Feilaktig innvilgelse
     */
    FEILAKTIG_INNVILG,

    /**
     * Ikke innvilget fra FK
     */
    FK_IKKE_INNV,

    /**
     * For høy uføregrad
     */
    FOR_HOY_UFOREGRAD,

    /**
     * For lav grad ved førsteuttak
     */
    FOR_LAV_UTG,

    /**
     * Gradsendring tilb. i tid
     */
    GRAD_TILB_I_TID,

    /**
     * Hovedytelsen er avslått
     */
    HOVEDYTELSE_AVSLAG,

    /**
     * Pensjon for lav v/67 år
     */
    LAVT_TIDLIG_UTTAK,

    /**
     * Ingen tilstøtende
     */
    MANGLER_EPS,

    /**
     * Har AFP
     */
    OFFAFP_OG_ALDER,

    /**
     * Samlet pensjon>100%
     */
    TOT_UTGRD_OVER_100,

    /**
     * §19-3 ikke oppfylt
     */
    UNDER_20_AR_BO,

    /**
     * §19-3 og §20-10 ikke oppfylt
     */
    UNDER_20_AR_BO_2016,

    /**
     * §19-3 ikke oppfylt
     */
    UNDER_20_AR_BO_3_AR,

    /**
     * Mindre enn 3 poengår
     */
    UNDER_3_AR_PP,

    /**
     * Trygdetid under 3 år
     */
    UNDER_3_AR_TT,

    /**
     * §20-10 ikke oppfylt
     */
    UNDER_20_AR_TT_2025,

    /**
     * Bruker er under 62 år
     */
    UNDER_62,

    /**
     * <1 år fra gradsendring
     */
    UTG_MINDRE_ETT_AR,

    /**
     * Ugyldig virkningsdato
     */
    VIRKFOM_FOR_2011,

    /**
     * VirkFOM for langt frem
     */
    VIRK_FOR_LANGT_FREM,

    /**
     * Bruker har mottatt uføretrygd etter fylte 62 år.
     */
    BRUKER_UT_E_62,

    /**
     * VirkFOM fra Fellesordningen er ulik ønsket virkFOM for kravet
     */
    VIRK_FRA_FO_ULIK,

    /**
     * Under ett års medlemskap
     */
    UNDER_1_AR_TT,

    /**
     * Hovedytelsen er opphørt
     */
    HOVEDYTELSE_OPPH,

    /**
     * Perioden skal ikke vurderes
     */
    PER_SKAL_IKKE_VURD,

    /**
     * Bruker forsørger ikke barnet
     */
    BRK_FORSO_IKKE_BARN,

    /**
     * Annen forelder har rett til barnetillegget
     */
    ANNEN_FORLD_RETT_BT,

    /**
     * Mindre enn 1 år siden BT ble flyttet mellom foreldrene
     */
    MINDRE_ETT_AR_BT_FLT,

    /**
     * Ikke mottatt dokumentasjon
     */
    IKKE_MOTTATT_DOK,

    /**
     * Bruker har flyttet til et ikke-avtale-land
     */
    BRUKER_FLYTTET_IKKE_AVT_LAND,

    /**
     * Barn har flyttet til et ikke-avtale-land
     */
    BARN_FLYTTET_IKKE_AVT_LAND,

    /**
     * Barn har oppholdt seg mer enn 90 dager i ikke-avtale-land
     */
    BARN_OPPH_IKKE_AVT_LAND,

    /**
     * EPS har flyttet til et ikke-avtale-land
     */
    EPS_FLYTTET_IKKE_AVT_LAND,

    /**
     * EPS har oppholdt seg mer enn 90 dager i ikke-avtale-land
     */
    EPS_OPPH_IKKE_AVT_LAND,

    /**
     *
     * Barnet over 18 år
     */
    BARN_OVER_18,

    /**
     * Over 18 år og utdanningskrav ikke oppfylt
     */
    BARN_OVER_18_IKKE_UTD_KRAV,

    /**
     * Barnet over 20/21 år
     */
    BARN_OVER_20,

    /**
     * Barnet har inntekt over 2G
     */
    BARN_INNT_OVER_2G,

    /**
     * Ikke godkjent dødsattest
     */
    IKKE_DODSATTEST,

    /**
     * Under 20 års botid
     */
    UNDER_20_AR_BOTID,

    /**
     * Eksportforbud
     */
    EKSPORT_FORBUD,

    /**
     * Trygdetid under 5 år
     */
    UNDER_5_AR_TT,

    /**
     * Forsørgingstillegg etter 2022
     */
    FT_ETTER_2022,

    /**
     * Gjenlevendetillegg fom 2024
     */
    GJT_FOM_2024,

    /**
     * Annet
     */
    ANNET
}
