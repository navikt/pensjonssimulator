package no.nav.pensjon.simulator.person.client.pdl.acl

import mu.KotlinLogging
import no.nav.pensjon.simulator.person.Sivilstandstype
import org.springframework.util.StringUtils.hasLength
import kotlin.also
import kotlin.collections.singleOrNull
import kotlin.collections.toTypedArray
import kotlin.text.equals

/**
 * For externalValue definitions see: https://pdldocs-navno.msappproxy.net/ekstern/index.html#_sivilstand
 */
enum class PdlSivilstandType(val externalValue: String, val internalValue: Sivilstandstype) {

    UNKNOWN("?", Sivilstandstype.UOPPGITT),
    UOPPGITT("UOPPGITT", Sivilstandstype.UOPPGITT),
    UGIFT("UGIFT", Sivilstandstype.UGIFT),
    GIFT("GIFT", Sivilstandstype.GIFT),
    ENKE_ELLER_ENKEMANN("ENKE_ELLER_ENKEMANN", Sivilstandstype.ENKE_ELLER_ENKEMANN),
    SKILT("SKILT", Sivilstandstype.SKILT),
    SEPARERT("SEPARERT", Sivilstandstype.SEPARERT),
    REGISTRERT_PARTNER("REGISTRERT_PARTNER", Sivilstandstype.REGISTRERT_PARTNER),
    SEPARERT_PARTNER("SEPARERT_PARTNER", Sivilstandstype.SEPARERT_PARTNER),
    SKILT_PARTNER("SKILT_PARTNER", Sivilstandstype.SKILT_PARTNER),
    GJENLEVENDE_PARTNER("GJENLEVENDE_PARTNER", Sivilstandstype.GJENLEVENDE_PARTNER);

    companion object {
        private val values = entries.toTypedArray()
        private val log = KotlinLogging.logger {}

        fun internalValue(value: String?): Sivilstandstype =
            fromExternalValue(value).internalValue

        private fun fromExternalValue(value: String?): PdlSivilstandType =
            values.singleOrNull { it.externalValue.equals(value, true) } ?: default(value)

        private fun default(externalValue: String?): PdlSivilstandType =
            if (hasLength(externalValue))
                UNKNOWN.also { log.warn { "Unknown PDL sivilstand '$externalValue'" } }
            else
                UOPPGITT
    }
}
