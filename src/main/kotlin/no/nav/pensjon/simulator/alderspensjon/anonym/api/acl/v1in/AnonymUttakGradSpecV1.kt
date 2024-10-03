package no.nav.pensjon.simulator.alderspensjon.anonym.api.acl.v1in

import mu.KotlinLogging
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import org.springframework.util.StringUtils

// no.nav.domain.pensjon.kjerne.kodetabeller.UttaksgradCode
enum class AnonymUttakGradSpecV1(val externalValue: String, val internalValue: UttakGradKode) {
    /**
     * 0 %
     */
    P_0("0", UttakGradKode.P_0),

    /**
     * 100 %
     */
    P_100("100", UttakGradKode.P_100),

    /**
     * 20 %
     */
    P_20("20", UttakGradKode.P_20),

    /**
     * 40 %
     */
    P_40("40", UttakGradKode.P_40),

    /**
     * 50 %
     */
    P_50("50", UttakGradKode.P_50),

    /**
     * 60 %
     */
    P_60("60", UttakGradKode.P_60),

    /**
     * 80 %
     */
    P_80("80", UttakGradKode.P_80);

    companion object {
        private val values = entries.toTypedArray()
        private val log = KotlinLogging.logger {}

        fun fromExternalValue(value: String?): AnonymUttakGradSpecV1 =
            values.singleOrNull { it.externalValue.equals(value, true) } ?: default(value)

        private fun default(externalValue: String?): AnonymUttakGradSpecV1 =
            if (StringUtils.hasLength(externalValue))
                P_100.also { log.warn { "Unknown AnonymUttakGradSpec: '$externalValue'" } }
            else
                P_100
    }
}
