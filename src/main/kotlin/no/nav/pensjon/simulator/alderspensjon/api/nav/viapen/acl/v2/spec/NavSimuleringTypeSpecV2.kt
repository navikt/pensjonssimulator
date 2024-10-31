package no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.spec

import mu.KotlinLogging
import no.nav.pensjon.simulator.core.domain.SimuleringType
import org.springframework.util.StringUtils.hasLength

// no.nav.domain.pensjon.kjerne.kodetabeller.SimuleringTypeCode
enum class NavSimuleringTypeSpecV2(val externalValue: String, val internalValue: SimuleringType) {
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
    ENDR_AP_M_AFP_PRIVAT("ENDR_AP_M_AFP_PRIVAT", SimuleringType.ENDR_AP_M_AFP_PRIVAT);

    /**
     * Gjenlevendepensjon
     */
    //GJENLEVENDE("GJENLEVENDE", SimuleringType.GJENLEVENDE);

    companion object {
        private val values = entries.toTypedArray()
        private val log = KotlinLogging.logger {}

        fun fromExternalValue(value: String?): NavSimuleringTypeSpecV2 =
            values.singleOrNull { it.externalValue.equals(value, true) } ?: default(value)

        private fun default(externalValue: String?): NavSimuleringTypeSpecV2 =
            if (hasLength(externalValue))
                ALDER.also { log.warn { "Unknown NavSimuleringTypeSpecV2: '$externalValue'" } }
            else
                ALDER
    }
}