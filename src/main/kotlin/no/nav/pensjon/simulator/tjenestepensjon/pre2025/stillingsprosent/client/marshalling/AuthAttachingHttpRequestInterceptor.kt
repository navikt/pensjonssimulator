package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling

import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import org.springframework.http.HttpHeaders
import org.springframework.ws.client.support.interceptor.ClientInterceptor
import org.springframework.ws.context.MessageContext
import org.springframework.ws.transport.context.TransportContextHolder
import org.springframework.ws.transport.http.HttpUrlConnection

class AuthAttachingHttpRequestInterceptor() : ClientInterceptor {

    override fun handleRequest(messageContext: MessageContext): Boolean {
        EgressAccess.token(service = EgressService.FSS_GATEWAY).value.let {
            val connection = TransportContextHolder.getTransportContext().connection as HttpUrlConnection
            connection.connection.addRequestProperty(HttpHeaders.AUTHORIZATION, "Bearer $it")
        }
        return true
    }

    override fun handleResponse(messageContext: MessageContext): Boolean = true

    override fun handleFault(messageContext: MessageContext): Boolean = true

    override fun afterCompletion(messageContext: MessageContext, ex: Exception?) {
    }
}