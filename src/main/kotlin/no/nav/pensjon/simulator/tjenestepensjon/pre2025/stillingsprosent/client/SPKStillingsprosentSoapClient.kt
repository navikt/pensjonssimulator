package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client

import mu.KotlinLogging
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.sporing.SporingsloggService
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.acl.Stillingsprosent
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.SOAPAdapter
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.SOAPCallback
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.request.FNR
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.request.HentStillingsprosentListeRequest
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.request.XMLHentStillingsprosentListeRequestWrapper
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.response.XMLHentStillingsprosentListeResponseWrapper
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.saml.SamlTokenClient
import no.nav.pensjon.simulator.tpregisteret.acl.TpOrdningFullDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.ws.client.WebServiceIOException
import org.springframework.ws.client.WebServiceTransportException
import org.springframework.ws.client.core.WebServiceTemplate
import org.springframework.ws.client.core.support.WebServiceGatewaySupport
import org.springframework.ws.soap.client.SoapFaultClientException

@Component
class SPKStillingsprosentSoapClient(
    private val webServiceTemplate: WebServiceTemplate,
    private val samlTokenClient: SamlTokenClient,
    private val sporingsloggService: SporingsloggService,
    @param:Value("\${spk.stillingsprosent.pre-2025.url}") private val url: String
) : WebServiceGatewaySupport() {
    private val log = KotlinLogging.logger {}

    fun getStillingsprosenter(
        fnr: String, tpOrdning: TpOrdningFullDto
    ): List<Stillingsprosent> {
        val dto = HentStillingsprosentListeRequest(FNR(fnr), tpOrdning)
        sporingsloggService.log(Pid(fnr), "", dto.toString()) //request ble sendt for å hente data
        log.info { "Henter stillingsprosenter for fnr $fnr fra $url" }
        try {
            val requestPayload: XMLHentStillingsprosentListeRequestWrapper = dto.let(SOAPAdapter::marshal)
            log.info { "RequestPayload to stillingsprosent url: $url payload: $requestPayload" }
            return webServiceTemplate.marshalSendAndReceive(
                requestPayload, SOAPCallback(url, samlTokenClient.samlAccessToken.accessToken)
            ).let {
                SOAPAdapter.unmarshal(it as XMLHentStillingsprosentListeResponseWrapper)
            }.stillingsprosentListe
        } catch (ex: WebServiceTransportException) {
            log.warn (ex) { "Transport error occurred while calling getStillingsprosenter: ${ex.message}" }
        } catch (ex: SoapFaultClientException) {
            // Handle SOAP faults returned from the server
            log.warn(ex) { "SOAP fault occurred at getStillingsprosenter: ${ex.faultStringOrReason}" }
            // Optionally, return a custom response or throw a custom exception
        } catch (ex: WebServiceIOException) {
            // Handle IO exceptions related to SOAP calls (e.g., timeout)
            log.warn(ex) { "IO error occurred while calling getStillingsprosenter: ${ex.message}" }
        } catch (ex: Exception) {
            log.warn(ex) { "Unexpected error occurred while calling getStillingsprosenter: ${ex.message}" }
        }
        return emptyList()
    }
}
