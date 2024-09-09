package no.nav.pensjon.simulator.core.beregn

// no.nav.domain.pensjon.kjerne.kodetabeller.InntektTypeCode
enum class InntektType(val value: String? = null) {
    /**
     * Antatt inntekt
     */
    AI,

    /**
     * Pensjonsgivende inntekt (Ligning)
     */
    ARBLIGN,

    /**
     * Arbeidsinntekt (Lønn og trekk)
     */
    ARBLTO,

    /**
     * Kapitalinntekt - bidrag SA
     */
    P_BI_x_KAP("BI-KAP"),

    /**
     * Ligningsinntekt (selvangivelse) - bidrag SA
     */
    P_BI_x_LIGN("BI-LIGN"),

    /**
     * Ligningsinntekt (lønn og trekk) - bidrag LT
     */
    P_BI_x_LTR("BI-LTR"),

    /**
     * Forventet bidrag o.l
     */
    FBI,

    /**
     * Forventet kapitalinntekt
     */
    FKI,

    /**
     * Forventet næringsinntekt
     */
    FORINTNAE,

    /**
     * Forventet arbeidsinntekt
     */
    FORINTARB,

    /**
     * Forventet utenlandsinntekt
     */
    FORINTUTL,

    /**
     * Forventet pensjongivende inntekt
     */
    FPI,

    /**
     * Hypotetisk forventet arbeidsinntekt
     */
    HYPF,

    /**
     * Hypotetisk forventet arbeidsinntekt 2G
     */
    HYPF2G,

    /**
     * Inntekt Mnd Før Uttak
     */
    IMFU,

    /**
     * Kapitalinntekt (ligning)
     */
    KAP,

    /**
     * Pensjonsgivende inntekt (lønn og trekk)
     */
    P_PE_x_PGILT("PE-PGILT"),

    /**
     * Stønadsinntekt inkludert fødsels- og sykepenger (lønn og trekk)
     */
    P_PE_x_SILT("PE-SILT"),

    /**
     * Pensjonsinntekt fra folketrygden
     */
    PENF,

    /**
     * Pensjonsinntekt (ikke folketrygd)
     */
    PENSKD,

    /**
     * Forventet pensjonsinntekt (ikke folketrygd)
     */
    PENT,

    /**
     * Forventet pensjonsinntekt (utland)
     */
    PENTU,

    /**
     * Foreløpig pensjonsgivende inntekt
     */
    PGI,

    /**
     * Sum av forventet arbeids-, kapital- og pensjonsinntekt
     */
    SFAKPI,

    /**
     * Ukjent inntektstype fra inntektsregisteret
     */
    UKJENT,

    /**
     * Inntekt til fradrag
     */
    IKKE_RED,

    /**
     * Innrapportert arbeidsinntekt
     */
    RAP_ARB,

    /**
     * Innrapportert næringsinntekt
     */
    RAP_NAR,

    /**
     * Forventet pensjon fra utlandet
     */
    FORPENUTL,

    /**
     * Forventet andre pensjoner og ytelser
     */
    FORINTAND,

    /**
     * Innrapporterte andre pensjoner og ytelser
     */
    RAP_AND,

    /**
     * Uføretrygd og evt. barnetillegg
     */
    UT_BT
}
