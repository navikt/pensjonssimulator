package no.nav.pensjon.simulator.core.domain.regler.enum

/**
 * pensjon-regler-api: no/nav/pensjon/regler/domain/enum/GrunnlagkildeEnum.kt
 * Copied 2025-08-26
 * Plus added comments
 */
enum class GrunnlagkildeEnum {
    /** Arbeidstaker- og arbeidsgiverregisteret */
    AA,

    /** Barnetrygd-systemet */
    BA,

    /** Batch */
    BATCH,

    /** Beregnet */
    BEREGNET,

    /** Bruker */
    BRUKER,

    /** Brukeroppgitt */
    BRUKER_OPP,

    /** Electronic Exchange of Social Security Information */
    EESSI,

    /** G-omregning */
    GOMR,

    /** Inntektskomponenten */
    IK,

    /** Inntektsregister */
    INNT,

    /** Inntekt satt ifm. konvertering */
    INNT_KONV,

    /** Institusjon */
    INSTOPP,

    /** Infotrygd */
    IT,

    /** Konvertering DSF */
    KONV_DSF,

    /** Data er konvertert inn fra både DSF og IT */
    KONV_DSF_IT,

    /** Konvertering Infotrygd */
    KONV_IT,

    /** Lønnsvekstomregn inntekter */
    LVOMR,

    /** Medlemsunntaksregister */
    MEDL,

    /** Minste Pensjonsnivå - Omregning */
    MPNOMR,

    /** Omsorgsopptjening */
    OMSORGSOPPTJENING,

    /** Øvrig */
    OVRIG,

    /** PEN fellesdata */
    PEN,

    /** Pensjonsopptjeningsregister */
    POPP,

    /** Prosess */
    PROSESS,

    /** Regulering av pensjon */
    REGULERING,

    /** PEN */
    SAKSB,

    /** Simulering */
    SIMULERING,

    /** Tjenestebasert persondatasystem (det gamle personregisteret) */
    TPS
}
