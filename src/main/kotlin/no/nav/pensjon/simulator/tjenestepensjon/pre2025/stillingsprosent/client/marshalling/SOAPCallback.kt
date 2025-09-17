package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.saml.SamlHeaderCallback
import org.springframework.ws.WebServiceMessage
import org.springframework.ws.client.core.WebServiceMessageCallback
import org.springframework.ws.soap.SoapMessage
import org.springframework.ws.soap.addressing.client.ActionCallback
import org.springframework.ws.soap.addressing.version.Addressing10
import org.springframework.ws.soap.client.core.SoapActionCallback
import java.net.URI

class SOAPCallback(url: String, samlToken: String) : WebServiceMessageCallback {

    private val wsAddressingCallback = ActionCallback(URI(""), Addressing10(), URI(url))
    private val soapActionCallback = SoapActionCallback(ACTION)
    private val samlHeaderCallback = SamlHeaderCallback(samlToken)

    override fun doWithMessage(message: WebServiceMessage) {
        wsAddressingCallback.doWithMessage(message as SoapMessage)
        soapActionCallback.doWithMessage(message)
        samlHeaderCallback.doWithMessage(message)
    }

    companion object {
        const val ACTION = "http://nav.no/ekstern/pensjon/tjenester/tjenestepensjonSimulering/v1/TjenestepensjonSimulering/hentStillingsprosentListeRequest"
    }
}