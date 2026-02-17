package no.nav.pensjon.simulator.tech.json

import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import no.nav.pensjon.simulator.tech.json.ObjectMapperConfiguration.Companion.dateSerializerModule
import org.springframework.boot.http.converter.autoconfigure.ServerHttpMessageConvertersCustomizer
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.converter.AbstractHttpMessageConverter
import org.springframework.http.converter.HttpMessageConverters
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import tools.jackson.core.json.JsonWriteFeature
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
        builder
            //.addCustomConverter(ByteArrayToStringConverter())
            .addCustomConverter(
                JacksonJsonHttpMessageConverter(
                    JsonMapper.builder().configure(JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS, true)
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

/**
 * Makes sure that the Swagger endpoint (swagger-ui/index.html) outputs a JSON payload.
 * Without this, the output will be a base64-encoded string.
 */
class ByteArrayToStringConverter : AbstractHttpMessageConverter<ByteArray>(MediaType.APPLICATION_JSON) {

    override fun supports(clazz: Class<*>): Boolean =
        ByteArray::class.java.isAssignableFrom(clazz)

    override fun readInternal(clazz: Class<out ByteArray>, inputMessage: HttpInputMessage): ByteArray =
        inputMessage.body.readAllBytes()

    override fun writeInternal(bytes: ByteArray, outputMessage: HttpOutputMessage) {
        outputMessage.body.write(bytes)
    }
}
