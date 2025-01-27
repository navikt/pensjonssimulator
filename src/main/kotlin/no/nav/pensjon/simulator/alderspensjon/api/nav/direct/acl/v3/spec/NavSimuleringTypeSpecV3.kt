package no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.spec

import mu.KotlinLogging
import no.nav.pensjon.simulator.core.domain.SimuleringType
import org.springframework.util.StringUtils.hasLength

/**
 * Corresponds to SimulatorSimuleringType in pensjonskalkulator-backend.
 */
// no.nav.domain.pensjon.kjerne.kodetabeller.SimuleringTypeCode
enum class NavSimuleringTypeSpecV3(val externalValue: String, val internalValue: SimuleringType) {
    /**
     * AFP
     */
    //AFP("AFP", SimuleringType.AFP),

    /**
     * AFP i offentlig sektor etterfulgt av alderspensjon
     */
    AFP_ETTERF_ALDER("AFP_ETTERF_ALDER", SimuleringType.AFP_ETTERF_ALDER),

    /**
     * AFP - vedtak om fremtidig pensjonspoeng
     */
    //AFP_FPP("AFP_FPP", SimuleringType.AFP_FPP),

    /**
     * Alderspensjon
     */
    ALDER("ALDER", SimuleringType.ALDER),

    /**
     * Alderspensjon
     */
    //ALDER_KAP_20("ALDER_KAP_20", SimuleringType.ALDER_KAP_20),

    /**
     * Alderspensjon med AFP i privat sektor
     */
    ALDER_M_AFP_PRIVAT("ALDER_M_AFP_PRIVAT", SimuleringType.ALDER_M_AFP_PRIVAT),

    ALDERSPENSJON_MED_LIVSVARIG_OFFENTLIG_AFP("ALDERSPENSJON_MED_LIVSVARIG_OFFENTLIG_AFP", SimuleringType.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG),

    /**
     * Alderspensjon med gjenlevenderettigheter
     */
    ALDER_M_GJEN("ALDER_M_GJEN", SimuleringType.ALDER_M_GJEN),

    /**
     * Barnepensjon
     */
    //BARN("BARN", SimuleringType.BARN),

    /**
     * Endring av alderspensjon
     */
    ENDR_ALDER("ENDR_ALDER", SimuleringType.ENDR_ALDER),

    /**
     * Endring av alderspensjon med gjenlevenderettigheter
     */
    ENDR_ALDER_M_GJEN("ENDR_ALDER_M_GJEN", SimuleringType.ENDR_ALDER_M_GJEN),

    /**
     * Endring av alderspensjon med AFP i privat sektor
     */
    ENDR_AP_M_AFP_PRIVAT("ENDR_AP_M_AFP_PRIVAT", SimuleringType.ENDR_AP_M_AFP_PRIVAT),

    ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG("ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG", SimuleringType.ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG);

    /**
     * Gjenlevendepensjon
     */
    //GJENLEVENDE("GJENLEVENDE", SimuleringType.GJENLEVENDE);

    companion object {
        private val log = KotlinLogging.logger {}

        @OptIn(ExperimentalStdlibApi::class)
        fun fromExternalValue(value: String?): NavSimuleringTypeSpecV3 =
            entries.singleOrNull { it.externalValue.equals(value, true) } ?: default(value)

        private fun default(externalValue: String?): NavSimuleringTypeSpecV3 =
            if (hasLength(externalValue))
                ALDER.also { log.warn { "Unknown NavSimuleringTypeSpecV3: '$externalValue'" } }
            else
                ALDER
    }
}
