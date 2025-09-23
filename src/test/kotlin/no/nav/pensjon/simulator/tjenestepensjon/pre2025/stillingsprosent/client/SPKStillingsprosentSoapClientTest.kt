package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.sporing.SporingsloggService
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.acl.Stillingsprosent
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.response.XMLHentStillingsprosentListeResponseWrapper
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.response.XMLHentStillingsprosentListeResponseWrapper.XMLHentStillingsprosentListeResponse
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.response.XMLStillingsprosent
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.saml.SamlToken
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.saml.SamlTokenClient
import no.nav.pensjon.simulator.tpregisteret.acl.TpOrdningFullDto
import org.springframework.ws.client.WebServiceIOException
import org.springframework.ws.client.WebServiceTransportException
import org.springframework.ws.client.core.WebServiceTemplate
import org.springframework.ws.soap.client.SoapFaultClientException
import java.time.LocalDate

class SPKStillingsprosentSoapClientTest : StringSpec({
    val webServiceTemplate = mockk<WebServiceTemplate>()
    val samlTokenClient = mockk<SamlTokenClient>()
    val sporingsloggService = mockk<SporingsloggService>()
    val client = SPKStillingsprosentSoapClient(webServiceTemplate, samlTokenClient, sporingsloggService, "some-url")

    val fnr = "12345678901"
    val tpOrdning = TpOrdningFullDto("pensjonskasse", "1234", LocalDate.now(), "867530")

    beforeEach {
        clearMocks(webServiceTemplate, samlTokenClient, sporingsloggService)
        every { samlTokenClient.samlAccessToken } returns SamlToken("token", 1L, "type")
        every { sporingsloggService.log(any(), any(), any()) } just runs
    }

    "getStillingsprosenter should return stillingsprosenter on success" {
        val expectedStillingsprosenter = listOf(
            Stillingsprosent(
                stillingsprosent = 100.0,
                datoFom = LocalDate.of(2020, 1, 1),
                datoTom = LocalDate.of(2021, 1, 1),
                faktiskHovedlonn = "100000",
                stillingsuavhengigTilleggslonn = "10000",
                aldersgrense = 70,
                utvidelse = null
            )
        )
        val responseWrapper = XMLHentStillingsprosentListeResponseWrapper(
        ).apply {
            response = XMLHentStillingsprosentListeResponse().apply {
                stillingsprosentListe = expectedStillingsprosenter.map { XMLStillingsprosent().apply {
                    stillingsprosent = it.stillingsprosent
                    datoFom = javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar(it.datoFom.toString())
                    datoTom = it.datoTom?.let { dt -> javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar(dt.toString()) }
                    faktiskHovedlonn = it.faktiskHovedlonn
                    stillingsuavhengigTilleggslonn = it.stillingsuavhengigTilleggslonn
                    aldersgrense = it.aldersgrense
                } }
            }
        }

        every { webServiceTemplate.marshalSendAndReceive(any<Any>(),
            any<org.springframework.ws.client.core.WebServiceMessageCallback>()) } returns responseWrapper

        val result = client.getStillingsprosenter(fnr, tpOrdning)

        result shouldBe expectedStillingsprosenter
        verify { sporingsloggService.log(Pid(fnr), "", any()) }
    }

    "getStillingsprosenter should return empty list on WebServiceTransportException" {
        every { webServiceTemplate.marshalSendAndReceive(any<Any>(),
            any<org.springframework.ws.client.core.WebServiceMessageCallback>()) } throws WebServiceTransportException("Transport error")

        val result = client.getStillingsprosenter(fnr, tpOrdning)

        result.shouldBeEmpty()
    }

    "getStillingsprosenter should return empty list on SoapFaultClientException" {
        val soapMessage = mockk<org.springframework.ws.soap.SoapMessage>(relaxed = true)
        val faultException = SoapFaultClientException(soapMessage)
        every { webServiceTemplate.marshalSendAndReceive(any<Any>(),
            any<org.springframework.ws.client.core.WebServiceMessageCallback>()) } throws faultException

        val result = client.getStillingsprosenter(fnr, tpOrdning)

        result.shouldBeEmpty()
    }

    "getStillingsprosenter should return empty list on WebServiceIOException" {
        every { webServiceTemplate.marshalSendAndReceive(any<Any>(),
            any<org.springframework.ws.client.core.WebServiceMessageCallback>()) } throws WebServiceIOException("IO error")

        val result = client.getStillingsprosenter(fnr, tpOrdning)

        result.shouldBeEmpty()
    }

    "getStillingsprosenter should return empty list on generic Exception" {
        every { webServiceTemplate.marshalSendAndReceive(any<Any>(),
            any<org.springframework.ws.client.core.WebServiceMessageCallback>()) } throws RuntimeException("Generic error")

        val result = client.getStillingsprosenter(fnr, tpOrdning)

        result.shouldBeEmpty()
    }

})
