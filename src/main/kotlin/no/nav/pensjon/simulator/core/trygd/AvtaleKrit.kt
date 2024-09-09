package no.nav.pensjon.simulator.core.trygd

// no.nav.domain.pensjon.kjerne.kodetabeller.AvtaleKritCode
enum class AvtaleKrit {
    /**
     * Ikke yrkesaktiv, 3 års botid
     */
    IK_YRK_BO,

    /**
     * Ikke yrkesaktiv, 1 års trygdetid
     */
    IK_YRK_TRYGD,

    /**
     * Omfattet av bestemmelsene i eksportavtale
     */
    OMF_BES_EKSP,

    /**
     * Yrkesaktiv, 1 års arbeid
     */
    YRK_ARB,

    /**
     * Yrkesaktiv i Norge eller EØS, ett års medlemskap i Norge
     */
    YRK_MEDL,

    /**
     * Yrkesaktiv, 1 års trygdetid
     */
    YRK_TRYGD,

    /**
     * Yrkesaktiv i Norge eller EØS
     */
    YRK_MEDL_ALT
}
