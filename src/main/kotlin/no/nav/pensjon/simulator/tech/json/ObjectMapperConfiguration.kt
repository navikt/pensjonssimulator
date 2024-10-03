package no.nav.pensjon.simulator.tech.json

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
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
open class ObjectMapperConfiguration {

    @Bean
    @Primary
    open fun objectMapper() =
        jacksonObjectMapper().apply {
            enable(SerializationFeature.INDENT_OUTPUT)
            registerModule(JavaTimeModule())
            registerModule(localDateModule())
            enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // for Date in call to PEN
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }

    // ConsPenReglerContextBeans.pensjonReglerObjectMapper
    @Bean("regler")
    open fun reglerObjectMapper(): ObjectMapper {
        val mapper = ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)

        mapper.configOverride(MutableMap::class.java).include = JsonInclude.Value.construct(
            JsonInclude.Include.NON_NULL,
            JsonInclude.Include.NON_NULL
        )

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

        mapper.setVisibility(
            mapper.serializationConfig.defaultVisibilityChecker
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
        )
        //mapper.addMixIn(VilkarsVedtak::class.java, VilkarsVedtakMixIn::class.java)
        //mapper.addMixIn(BeregningRelasjon::class.java, BeregningRelasjonMixIn::class.java)
        //mapper.addMixIn(Beregning::class.java, BeregningMixIn::class.java)
        //mapper.addMixIn(Beregning2011::class.java, Beregning2011MixIn::class.java)
        //mapper.addMixIn(PensjonUnderUtbetaling::class.java, PensjonUnderUtbetalingMixIn::class.java)
        //mapper.addMixIn(Tilleggspensjon::class.java, FormlerMixIn::class.java)
        mapper.registerModule(JavaTimeModule())
        return mapper
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
