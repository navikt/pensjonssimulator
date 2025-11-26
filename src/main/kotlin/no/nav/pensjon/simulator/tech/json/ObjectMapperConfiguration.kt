package no.nav.pensjon.simulator.tech.json

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.json.JsonMapper

/**
 * Configuration of object mappers used for serialization and deserialization of data in JSON format.
 */
@Configuration
open class ObjectMapperConfiguration {

    @Bean
    open fun jsonMapper(): JsonMapper =
        JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_NULL) }
            .build()
}
