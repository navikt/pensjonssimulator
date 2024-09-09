package no.nav.pensjon.simulator.core.beholdning

// no.nav.domain.pensjon.kjerne.kodetabeller.SatsTypeCode
enum class SatsType {
    /**
     * Garantipensjonsnivåsats
     */
    GPNSATS,

    /**
     * Grunnbeløp
     */
    GRUNNBELOP,

    /**
     * Grunnpensjonsats
     */
    GRUNNPENSJONSATS,

    /**
     * Lønnsvekst
     */
    LONNSVEKST,

    /**
     * Minsteytelse uføretrygdsats
     */
    MINSTEYTELSEUTSATS,

    /**
     * Minste pensjonsnivåsats
     */
    MPNSATS,

    /**
     * Reguleringsfaktor
     */
    REGULERINGSFAKTOR,

    /**
     * Særtilleggsats
     */
    SARTILLEGGSATS,

    /**
     * Skjermingsgrad
     */
    SKJERMINGSGRAD,

    /**
     * Sats for rettsgebyr
     */
    RETTSGEBYR,

    /**
     * Terskelverdi for feilutbetalinger
     */
    TERSKEL_FEILUTBET,

    /**
     * Toleransegrense for etterbetaling i etteroppgjør
     */
    TOL_GR_EO_ETTERBET,

    /**
     * Toleransegrense for tilbakekreving i etteroppgjør
     */
    TOL_GR_EO_TILBAKEKR
}
