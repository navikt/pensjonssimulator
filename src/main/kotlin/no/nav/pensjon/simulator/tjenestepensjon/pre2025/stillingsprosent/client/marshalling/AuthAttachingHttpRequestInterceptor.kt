package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling

import mu.KotlinLogging
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import org.springframework.ws.client.support.interceptor.ClientInterceptor
import org.springframework.ws.context.MessageContext
import org.springframework.ws.transport.context.TransportContextHolder
import org.springframework.ws.transport.http.HttpUrlConnection

class AuthAttachingHttpRequestInterceptor() : ClientInterceptor {
    private val logger = KotlinLogging.logger {}

    override fun handleRequest(messageContext: MessageContext): Boolean {
        EgressAccess.token(EgressService.FSS_GATEWAY).value.let {
            val transportContext = TransportContextHolder.getTransportContext()
            val connection = transportContext.connection as HttpUrlConnection
            connection.connection.addRequestProperty("Authorization", "Bearer $it")
            logger.info { "Attaching token to SOAP request" }
        }
        return true
    }

    override fun handleResponse(messageContext: MessageContext): Boolean {
        return true
    }

    override fun handleFault(messageContext: MessageContext): Boolean {
        return true
    }

    override fun afterCompletion(messageContext: MessageContext, ex: Exception?) {
    }
}