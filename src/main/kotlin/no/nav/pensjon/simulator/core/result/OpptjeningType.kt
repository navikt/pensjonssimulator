package no.nav.pensjon.simulator.core.result

// no.nav.domain.pensjon.kjerne.kodetabeller.OpptjeningTypeCode
enum class OpptjeningType {
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
     * Pensjonsgivende inntekt
     */
    PPI
}
