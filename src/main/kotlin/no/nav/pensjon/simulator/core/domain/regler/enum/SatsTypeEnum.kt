package no.nav.pensjon.simulator.core.domain.regler.enum

/**
 * pensjon-regler-api: no/nav/pensjon/regler/domain/enum/SatsTypeEnum.kt
 * 2025-01-29
 */
enum class SatsTypeEnum {
    LONNSVEKST,
    GRUNNBELOP,
    LONNSVEKSTSATS,
    MPNSATS,
    REGULERINGSFAKTORSATS,
    SARTILLEGGSATS,
    SKJERMINGSTILLEGGSATS,
    VEIETGRUNNBELOPSATS,
    GPNSATS,
    GRUNNPENSJONSATS,
    MINSTEYTELSEUTSATS,
    RETTSGEBYR,
    TOL_GR_EO_ETTERBET,
    TOL_GR_EO_TILBAKEKR,
    TERSKEL_FEILUTBET,

    /**
     * Fra PEN
     */
    REGULERINGSFAKTOR,
    SKJERMINGSGRAD
}
