package no.nav.pensjon.simulator.tech.json

import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ser.std.StdSerializer
import java.util.Date

/**
 * Serializes java.util.Date as epoch milliseconds.
 * Used for calls to pensjon-regler.
 */
class EpochDateSerializer(handledType: Class<Date>) : StdSerializer<Date>(handledType) {

    override fun serialize(value: Date, gen: JsonGenerator, provider: SerializationContext) {
        gen.writeNumber(value.toInstant().toEpochMilli())
    }
}
