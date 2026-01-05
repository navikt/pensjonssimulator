package no.nav.pensjon.simulator.tech.json

import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleModule
import java.util.*

/**
 * Configuration of object mappers used for serialization and deserialization of data in JSON format.
 */
@Configuration
open class ObjectMapperConfiguration {

    @Bean
    open fun jsonMapper(): JsonMapper =
        JsonMapper.builder()
            .addModule(dateSerializerModule())
            .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .changeDefaultPropertyInclusion { it.withValueInclusion(NON_NULL) }
            .build()

    companion object {

        fun dateSerializerModule() =
            SimpleModule().apply {
                addSerializer(EpochDateSerializer(handledType = Date::class.java))
            }
    }
}
