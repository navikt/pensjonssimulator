package no.nav.pensjon.simulator.tech.json

import no.nav.pensjon.simulator.person.FoedselsnummerUtil
import tools.jackson.databind.json.JsonMapper

fun JsonMapper.writeValueAsRedactedString(value: Any): String =
    try {
        FoedselsnummerUtil.redact(this.writeValueAsString(value))
    } catch (_: Exception) {
        // should not fail the request
        "Failed to redact value"
    }
