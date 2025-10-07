package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.response

import jakarta.xml.bind.annotation.*

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = ["response"])
@XmlRootElement(name = "hentStillingsprosentListeResponse", namespace = "http://nav.no/ekstern/pensjon/tjenester/tjenestepensjonSimulering/v1")
class XMLHentStillingsprosentListeResponseWrapper {
    @XmlElement(required = true)
    lateinit var response: XMLHentStillingsprosentListeResponse

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = ["stillingsprosentListe"])
    class XMLHentStillingsprosentListeResponse {
        lateinit var stillingsprosentListe: List<XMLStillingsprosent>
    }
}