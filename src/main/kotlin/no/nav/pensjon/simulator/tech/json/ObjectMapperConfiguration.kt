package no.nav.pensjon.simulator.tech.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.pensjon.simulator.tech.time.DateUtil.toLocalDate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Configuration of object mapper used for serialization and deserialization of data in JSON and XML format.
 */
@Configuration
class ObjectMapperConfiguration {

    @Bean
    @Primary
    fun objectMapper() =
        jacksonObjectMapper().apply {
            enable(SerializationFeature.INDENT_OUTPUT)
            registerModule(JavaTimeModule())
            registerModule(localDateModule())
            enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // for Date in call to PEN
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }

    private fun localDateModule() =
        SimpleModule().apply {
            addDeserializer(LocalDate::class.java, LocalDateDeserializer())
        }

    /**
     * Produces date 2024-02-01 both from
     * "date": "2024-01-31T23:00:00.000Z"
     * and
     * "date": "2024-02-01"
     */
    class LocalDateDeserializer : JsonDeserializer<LocalDate>() {
        override fun deserialize(parser: JsonParser, context: DeserializationContext): LocalDate =
            parser.text.let {
                try {
                    LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
                } catch (e: DateTimeParseException) {
                    toLocalDate(ZonedDateTime.parse(it, DateTimeFormatter.ISO_ZONED_DATE_TIME))
                }
            }
    }
}
