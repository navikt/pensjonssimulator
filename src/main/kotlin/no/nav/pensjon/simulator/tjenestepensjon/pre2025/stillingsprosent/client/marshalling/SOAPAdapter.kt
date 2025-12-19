package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling

import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.acl.Stillingsprosent
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.request.FNR
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.request.HentStillingsprosentListeRequest
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.request.XMLHentStillingsprosentListeRequestWrapper
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.response.HentStillingsprosentListeResponse
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.response.XMLHentStillingsprosentListeResponseWrapper
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.response.XMLStillingsprosent
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.response.XmlFaultWrapper

object SOAPAdapter {

    private fun Stillingsprosent.toXML() = XMLStillingsprosent().also {
        it.aldersgrense = aldersgrense
        it.datoFom = datoFom.toXMLGregorianCalendar()
        it.datoTom = datoTom?.toXMLGregorianCalendar()
        it.faktiskHovedlonn = faktiskHovedlonn
        it.stillingsprosent = stillingsprosent
        it.stillingsuavhengigTilleggslonn = stillingsuavhengigTilleggslonn
    }

    fun marshal(p0: HentStillingsprosentListeRequest): XMLHentStillingsprosentListeRequestWrapper = with(p0) {
        XMLHentStillingsprosentListeRequestWrapper().also { wrapper ->
            wrapper.request = XMLHentStillingsprosentListeRequestWrapper.XMLHentStillingsprosentListeRequest().also {
                it.tssEksternId = tssEksternId
                it.fnr = fnr.toString()
                it.simuleringsKode = simuleringsKode
                it.tpnr = tpnr
            }
        }
    }

    fun unmarshal(p0: XMLHentStillingsprosentListeRequestWrapper): HentStillingsprosentListeRequest = with(p0.request) {
        HentStillingsprosentListeRequest(
                tssEksternId = this?.tssEksternId,
                fnr = FNR(this?.fnr),
                simuleringsKode = this?.simuleringsKode,
                tpnr = this?.tpnr
        )
    }

    fun marshal(p0: HentStillingsprosentListeResponse): XMLHentStillingsprosentListeResponseWrapper = with(p0) {
        XMLHentStillingsprosentListeResponseWrapper().also { wrapper ->
            wrapper.response = XMLHentStillingsprosentListeResponseWrapper.XMLHentStillingsprosentListeResponse().also {
                it.stillingsprosentListe = stillingsprosentListe.map { o -> o.toXML() }
            }
        }
    }

    fun unmarshal(result: XMLHentStillingsprosentListeResponseWrapper): List<Stillingsprosent> =
        result.response.stillingsprosentListe.map(XMLStillingsprosent::toStillingsprosent)

    fun handleFault(fault: XmlFaultWrapper): List<Stillingsprosent> =
        throw EgressException(fault.toFault().toString())
}
