package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.saml

import org.springframework.ws.WebServiceMessage
import org.springframework.ws.client.core.WebServiceMessageCallback
import org.springframework.ws.soap.SoapMessage
import org.springframework.xml.transform.StringSource
import java.util.*
import javax.xml.namespace.QName
import javax.xml.transform.TransformerFactory

class SamlHeaderCallback(private val token: String) : WebServiceMessageCallback {

    private val transformer = TransformerFactory.newInstance().newTransformer()

    override fun doWithMessage(message: WebServiceMessage) {
        val soapMessage = message as SoapMessage

        // Safely add header without affecting Content-Length
        val headerElement = soapMessage.soapHeader.addHeaderElement(
            QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security")
        )

        // Use the transformer to ensure message integrity
        transformer.transform(StringSource(decodeToken(token)), headerElement.result)
    }
    companion object {
        fun decodeToken(token: String): String {
            return if (token.contains("-")) String(Base64.getUrlDecoder().decode(token))
            else String(Base64.getDecoder().decode(token))
        }
    }
}