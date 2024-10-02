package no.nav.pensjon.simulator.core.domain.regler.enum

// TODO Tatt fra Pesys. Ikke noe eksisterende som matcher med VilkarVurderingCti
enum class VilkarVurderingEnum {
    /**
     * Egne barn - Overgangsstønad
     */
    EGNE_BARN_FP,
    /**
     * Egne barn - Overgangsstønad
     */
    EGNE_BARN_GJP,
    /**
     * Egne barn - Overgangsstønad
     */
    EGNE_BARN_GJR,
    /**
     * Egne barn
     */
    EGNE_BARN_KUN_FP,
    /**
     * Egne barn
     */
    EGNE_BARN_KUN_GJP,
    /**
     * Egne barn
     */
    EGNE_BARN_KUN_GJR,
    /**
     * Innvilget § 18-2
     */
    HALV_MINPEN_BP,
    /**
     * Innvilget § 17-3
     */
    HALV_MINPEN_GJP,
    /**
     * Innvilget § 17-3
     */
    HALV_MINPEN_GJR,
    /**
     * Innvilget § 17-10.1a
     */
    INNV1,
    /**
     * Innvilget §17-10.1a
     */
    INNV1_GJR,
    /**
     * Innvilget § 17-10.1b
     */
    INNV2,
    /**
     * Innvilget §17-10.1b
     */
    INNV2_GJR,
    /**
     * Innvilget § 17-10.1c
     */
    INNV3,
    /**
     * Innvilget § 17-10.1c
     */
    INNV3_GJR,
    /**
     * Innvilget § 17-10.2
     */
    INNV4,
    /**
     * Innvilget § 17- 10.2
     */
    INNV4_GJR,
    /**
     * Innvilget gammel § 10-6 (før 1989)
     */
    INNV5,
    /**
     * Innvilget gammel §10-16 (før 1989)
     */
    INNV5_GJR,
    /**
     * Omstillingsperiode - Overgangsstønad
     */
    OMSTILL_FP,
    /**
     * Omstillingsperiode - Overgangsstønad
     */
    OMSTILL_GJP,
    /**
     * Omstillingsperiode - Overgangsstønad
     */
    OMSTILL_GJR,
    /**
     * Omsorg avdødes barn - Overgangsstønad
     */
    OMS_AVD_BARN_FP,
    /**
     * Omsorg avdødes barn - Overgangsstønad
     */
    OMS_AVD_BARN_GJP,
    /**
     * Omsorg avdødes barn - Overgangsstønad
     */
    OMS_AVD_BARN_GJR,
    /**
     * Omsorg avdødes barn
     */
    OMS_AVD_BARN_KUN_FP,
    /**
     * Omsorg avdødes barn
     */
    OMS_AVD_BARN_KUN_GJP,
    /**
     * Omsorg avdødes barn
     */
    OMS_AVD_BARN_KUN_GJR,
    /**
     * Lærling/praktikant
     */
    PRAKTIK,
    /**
     * Under utdanning
     */
    UNDER_UTDAN,
    /**
     * Nødvendig utdanning - Overgangsstønad
     */
    UTDAN_FP,
    /**
     * Nødvendig utdanning - Overgangsstønad
     */
    UTDAN_GJP,
    /**
     * Nødvendig utdanning - Overgangsstønad
     */
    UTDAN_GJR
}
