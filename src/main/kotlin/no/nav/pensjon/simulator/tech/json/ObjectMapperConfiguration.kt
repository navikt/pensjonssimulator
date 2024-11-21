package no.nav.pensjon.simulator.tech.json

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

/**
 * Configuration of object mapper used for serialization and deserialization of data in JSON format.
 */
@Configuration
open class ObjectMapperConfiguration {

    @Bean
    @Primary
    open fun objectMapper() =
        jacksonObjectMapper().apply {
            enable(SerializationFeature.INDENT_OUTPUT)
            registerModule(JavaTimeModule())
            enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // for Date in call to PEN
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }

    // ConsPenReglerContextBeans.pensjonReglerObjectMapper
    @Bean("regler")
    open fun reglerObjectMapper() =
        ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false).apply {

                configOverride(MutableMap::class.java).include = JsonInclude.Value.construct(
                    JsonInclude.Include.NON_NULL,
                    JsonInclude.Include.NON_NULL
                )

                setSerializationInclusion(JsonInclude.Include.NON_NULL)

                setVisibility(
                    serializationConfig.defaultVisibilityChecker
                        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
                )

                registerModule(JavaTimeModule())
            }
}
