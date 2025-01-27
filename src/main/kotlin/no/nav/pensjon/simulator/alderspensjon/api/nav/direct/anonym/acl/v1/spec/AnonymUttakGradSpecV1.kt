package no.nav.pensjon.simulator.alderspensjon.api.nav.direct.anonym.acl.v1.spec

import mu.KotlinLogging
import no.nav.pensjon.simulator.alderspensjon.api.nav.direct.anonym.acl.v1.spec.AnonymUttakGradSpecV1.entries
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import org.springframework.util.StringUtils.hasLength

/**
 * Corresponds to SimulatorUttaksgrad in pensjonskalkulator-backend.
 */
enum class AnonymUttakGradSpecV1(val externalValue: String, val internalValue: UttakGradKode) {

    NULL(externalValue = "P_0", internalValue = UttakGradKode.P_0),
    TJUE_PROSENT(externalValue = "P_20", internalValue = UttakGradKode.P_20),
    FOERTI_PROSENT(externalValue = "P_40", internalValue = UttakGradKode.P_40),
    FEMTI_PROSENT(externalValue = "P_50", internalValue = UttakGradKode.P_50),
    SEKSTI_PROSENT(externalValue = "P_60", internalValue = UttakGradKode.P_60),
    AATTI_PROSENT(externalValue = "P_80", internalValue = UttakGradKode.P_80),
    HUNDRE_PROSENT(externalValue = "P_100", internalValue = UttakGradKode.P_100);

    companion object {
        private val log = KotlinLogging.logger {}

        @OptIn(ExperimentalStdlibApi::class)
        fun fromExternalValue(value: String?): AnonymUttakGradSpecV1 =
            entries.singleOrNull { it.externalValue.equals(value, true) } ?: default(value)

        private fun default(externalValue: String?): AnonymUttakGradSpecV1 =
            if (hasLength(externalValue))
                HUNDRE_PROSENT.also { log.warn { "Unknown AnonymUttakGradSpec: '$externalValue'" } }
            else
                HUNDRE_PROSENT
    }
}
