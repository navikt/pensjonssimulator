package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling

import jakarta.xml.bind.JAXBElement
import mu.KotlinLogging
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.error.SoapFaultException
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.error.SoapFaultElement
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.stereotype.Component
import org.springframework.ws.WebServiceMessage
import org.springframework.ws.client.core.FaultMessageResolver
import org.springframework.ws.soap.SoapFault
import org.springframework.ws.soap.SoapMessage
import javax.xml.transform.Source

@Component
class SoapFaultHandler(private val jaxb2Marshaller: Jaxb2Marshaller) : FaultMessageResolver {
    private val log = KotlinLogging.logger {}

    override fun resolveFault(message: WebServiceMessage) =
        throw (message as SoapMessage).soapBody.fault?.let(::faultException)
            ?: SoapFaultException("WebServiceMessage", message.toString())

    private fun faultException(fault: SoapFault): SoapFaultException? =
        try {
            fault.faultDetail?.detailEntries?.next()?.source?.let {
                faultException(faultElement(source = it).value)
            }
        } catch (e: Exception) {
            faultException(fault, e)
        }

    private fun faultException(faultElement: SoapFaultElement) =
        SoapFaultException(faultElement::class.qualifiedName ?: "", faultElement.errorMessage)
            .also {
                log.warn { "Resolved known fault from SoapFaultDetail: $it" }
            }

    private fun faultException(fault: SoapFault, e: Exception) =
        SoapFaultException(fault.faultCode.toString(), fault.faultStringOrReason ?: "")
            .also {
                log.warn(e) { "Could not resolve known error from SoapFaultDetail. Resolved from SoapFault: $it" }
            }

    @Suppress("UNCHECKED_CAST")
    private fun faultElement(source: Source) =
        jaxb2Marshaller.unmarshal(source) as JAXBElement<SoapFaultElement>
}