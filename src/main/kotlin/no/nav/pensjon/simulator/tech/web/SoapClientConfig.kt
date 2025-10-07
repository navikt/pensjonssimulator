package no.nav.pensjon.simulator.tech.web

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.response.XMLStillingsprosent
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.request.XMLHentStillingsprosentListeRequestWrapper
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.response.XMLHentStillingsprosentListeResponseWrapper
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.AuthAttachingHttpRequestInterceptor
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.SoapFaultHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.ws.client.core.WebServiceTemplate
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender

@Configuration
open class SoapClientConfig(
    @param:Value("\${stillingsprosent.url}") val baseUrl: String,
) {

    @Bean
    open fun jaxb2Marshaller() = Jaxb2Marshaller().apply {
        setClassesToBeBound(
                XMLHentStillingsprosentListeRequestWrapper::class.java,
                XMLHentStillingsprosentListeResponseWrapper::class.java,
                XMLStillingsprosent::class.java)
    }

    @Bean
    open fun webServiceTemplate(jaxb2Marshaller: Jaxb2Marshaller) =
        WebServiceTemplate().apply {
            defaultUri = "$baseUrl$PATH"
            marshaller = jaxb2Marshaller
            unmarshaller = jaxb2Marshaller
            faultMessageResolver = SoapFaultHandler(jaxb2Marshaller)
            interceptors = arrayOf(
                AuthAttachingHttpRequestInterceptor(),
            )
            setMessageSender(HttpUrlConnectionMessageSender())
            setCheckConnectionForFault(true)
            setCheckConnectionForError(true)
        }

    companion object {
        const val ENCODING = "UTF-8"
        const val PATH = "/ekstern-pensjon-tjeneste-tjenestepensjonSimuleringWeb/sca/TjenestepensjonSimuleringWSEXP"
    }
}