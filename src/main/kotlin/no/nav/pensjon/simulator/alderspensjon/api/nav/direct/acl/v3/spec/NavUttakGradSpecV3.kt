package no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.spec

import mu.KotlinLogging
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import org.springframework.util.StringUtils.hasLength

// no.nav.domain.pensjon.kjerne.kodetabeller.UttaksgradCode
enum class NavUttakGradSpecV3(val externalValue: String, val internalValue: UttakGradKode) {
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
        private val log = KotlinLogging.logger {}

        @OptIn(ExperimentalStdlibApi::class)
        fun fromExternalValue(value: String?): NavUttakGradSpecV3 =
            entries.singleOrNull { it.externalValue.equals(value, true) } ?: default(value)

        private fun default(externalValue: String?): NavUttakGradSpecV3 =
            if (hasLength(externalValue))
                P_100.also { log.warn { "Unknown AnonymUttakGradSpec: '$externalValue'" } }
            else
                P_100
    }
}
