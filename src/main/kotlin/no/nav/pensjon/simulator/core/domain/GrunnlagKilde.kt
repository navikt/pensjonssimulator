package no.nav.pensjon.simulator.core.domain

// no.nav.domain.pensjon.kjerne.kodetabeller.GrunnlagKildeCode
enum class GrunnlagKilde {
    /** AA-registeret */
    AA,

    /** Batch */
    BATCH,

    /** Beregnet */
    BEREGNET,

    /** Bruker */
    BRUKER,

    /** Brukeroppgitt */
    BRUKER_OPP,

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

    /** TPS - Personregister */
    TPS,

    /** BA - Barnetrygd Systemet */
    BA,

    /** OMSORGSOPPTJENING - Omsorgsopptjening */
    OMSORGSOPPTJENING
}
