package no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.spec

import mu.KotlinLogging
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import org.springframework.util.StringUtils.hasLength

// no.nav.domain.pensjon.kjerne.kodetabeller.SivilstatusTypeCode
enum class NavSivilstandSpecV3(val externalValue: String, val internalValue: SivilstatusType) {
    /**
     * Enke/-mann
     */
    ENKE("ENKE", SivilstatusType.ENKE),

    /**
     * Gift
     */
    GIFT("GIFT", SivilstatusType.GIFT),

    /**
     * Gjenlevende etter samlivsbrudd
     */
    GJES("GJES", SivilstatusType.GJES),

    /**
     * Gjenlevende partner
     */
    GJPA("GJPA", SivilstatusType.GJPA),

    /**
     * Gjenlevende samboer
     */
    GJSA("GJSA", SivilstatusType.GJSA),

    /**
     * Gift, lever adskilt
     */
    GLAD("GLAD", SivilstatusType.GLAD),

    /**
     * -
     */
    NULL("NULL", SivilstatusType.NULL),

    /**
     * Registrert partner, lever adskilt
     */
    PLAD("PLAD", SivilstatusType.PLAD),

    /**
     * Registrert partner
     */
    REPA("REPA", SivilstatusType.REPA),

    /**
     * Samboer
     */
    SAMB("SAMB", SivilstatusType.SAMB),

    /**
     * Separert partner
     */
    SEPA("SEPA", SivilstatusType.SEPA),

    /**
     * Separert
     */
    SEPR("SEPR", SivilstatusType.SEPR),

    /**
     * Skilt
     */
    SKIL("SKIL", SivilstatusType.SKIL),

    /**
     * Skilt partner
     */
    SKPA("SKPA", SivilstatusType.SKPA),

    /**
     * Ugift
     */
    UGIF("UGIF", SivilstatusType.UGIF);

    companion object {
        private val log = KotlinLogging.logger {}

        @OptIn(ExperimentalStdlibApi::class)
        fun fromExternalValue(value: String?): NavSivilstandSpecV3 =
            entries.singleOrNull { it.externalValue.equals(value, true) } ?: default(value)

        private fun default(externalValue: String?): NavSivilstandSpecV3 =
            if (hasLength(externalValue))
                UGIF.also { log.warn { "Unknown NavSivilstandSpecV3: '$externalValue'" } }
            else
                UGIF
    }
}
