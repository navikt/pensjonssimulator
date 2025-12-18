package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.response

import jakarta.xml.bind.annotation.*
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.acl.Fault

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = ["faultcode", "faultstring", "detail"])
@XmlRootElement(name = "Fault", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
class XmlFaultWrapper {
    @XmlElement(required = true)
    var faultcode: String? = null

    @XmlElement(required = false)
    var faultstring: String? = null

    @XmlElement(required = false)
    var detail: XmlDetail? = null

    fun toFault() =
        Fault(
            code = faultcode ?: "",
            message = faultstring ?: "",
            detail = detail?.hentStillingsprosentListestillingsprosentListeKanIkkeLeveres?.errorMessage ?: ""
        )
}

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = ["hentStillingsprosentListestillingsprosentListeKanIkkeLeveres"])
class XmlDetail {
    @XmlElement(required = true, namespace = "http://nav.no/ekstern/pensjon/tjenester/tjenestepensjonSimulering/v1")
    var hentStillingsprosentListestillingsprosentListeKanIkkeLeveres: XmlErrorMessage? = null
}

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = ["errorMessage"])
class XmlErrorMessage {
    @XmlElement(required = true)
    var errorMessage: String? = null
}
