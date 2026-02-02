package no.nav.pensjon.simulator.tech.json

import no.nav.pensjon.simulator.person.FoedselsnummerUtil
import tools.jackson.databind.json.JsonMapper

fun JsonMapper.writeValueAsRedactedString(value: Any) =
    FoedselsnummerUtil.redact(this.writeValueAsString(value))

