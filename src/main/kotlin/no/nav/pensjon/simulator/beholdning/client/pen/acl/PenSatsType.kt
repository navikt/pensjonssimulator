package no.nav.pensjon.simulator.beholdning.client.pen.acl

import mu.KotlinLogging
import no.nav.pensjon.simulator.beholdning.SatsType
import org.springframework.util.StringUtils.hasLength

enum class PenSatsType(val externalValue: String, val internalValue: SatsType) {
    NONE("", SatsType.NONE),
    UNKNOWN("?", SatsType.UNKNOWN),
    ORDINAER("ORDINAER", SatsType.ORDINAER),
    HOEY("HOY", SatsType.HOEY);

    companion object {
        private val values = entries.toTypedArray()
        private val log = KotlinLogging.logger {}

        fun fromExternalValue(value: String?) =
            values.singleOrNull { it.externalValue.equals(value, true) } ?: default(value)

        private fun default(externalValue: String?) =
            if (hasLength(externalValue))
                UNKNOWN.also { log.warn { "Unknown PEN satstype '$externalValue'" } }
            else
                NONE
    }

}
