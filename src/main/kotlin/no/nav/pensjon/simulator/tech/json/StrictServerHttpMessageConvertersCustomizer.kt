package no.nav.pensjon.simulator.tech.json

import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import no.nav.pensjon.simulator.tech.json.ObjectMapperConfiguration.Companion.dateSerializerModule
import org.springframework.boot.http.converter.autoconfigure.ServerHttpMessageConvertersCustomizer
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverters
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.json.JsonMapper
import java.text.SimpleDateFormat

@Configuration
open class StrictServerHttpMessageConvertersCustomizer : ServerHttpMessageConvertersCustomizer {

    /**
     * Configure the message converter used in REST controllers.
     * Using a strict mapper so that clients get an error message in case of wrong JSON property names.
     */
    override fun customize(builder: HttpMessageConverters.ServerBuilder) {
        builder.addCustomConverter(
            JacksonJsonHttpMessageConverter(
                JsonMapper.builder()
                    .addModule(dateSerializerModule())
                    .defaultDateFormat(SimpleDateFormat("yyyy-MM-dd"))
                    .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES) // i.e. strict
                    .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
                    .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                    .changeDefaultPropertyInclusion { it.withValueInclusion(NON_NULL) }
                    .build()
            )
        )
    }
}
