package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.acl.Stillingsprosent
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.SPKStillingsprosentSoapClient
import no.nav.pensjon.simulator.tpregisteret.TpOrdningFullDto
import java.time.LocalDate

class SPKStillingsprosentServiceTest : FunSpec({

    val fnr = "12345678910"

    fun tpOrdning() = TpOrdningFullDto(
        navn = "SPK",
        tpNr = "3010",
        tssId = "tss-3010"
    )

    fun stillingsprosent(prosent: Double = 100.0) = Stillingsprosent(
        datoFom = LocalDate.of(2020, 1, 1),
        datoTom = null,
        stillingsprosent = prosent,
        aldersgrense = 67,
        faktiskHovedlonn = "500000",
        stillingsuavhengigTilleggslonn = null,
        utvidelse = null
    )

    fun service(
        client: SPKStillingsprosentSoapClient = mockk()
    ) = SPKStillingsprosentService(client)

    // --- Happy path ---

    test("getStillingsprosentListe returns list from client") {
        val expected = listOf(stillingsprosent(100.0), stillingsprosent(50.0))
        val client = mockk<SPKStillingsprosentSoapClient> {
            every { getStillingsprosenter(fnr, any()) } returns expected
        }

        val result = service(client).getStillingsprosentListe(fnr, tpOrdning())

        result shouldBe expected
    }

    test("getStillingsprosentListe passes fnr and tpOrdning to client") {
        val ordning = tpOrdning()
        val client = mockk<SPKStillingsprosentSoapClient> {
            every { getStillingsprosenter(fnr, ordning) } returns emptyList()
        }

        service(client).getStillingsprosentListe(fnr, ordning)

        verify { client.getStillingsprosenter(fnr, ordning) }
    }

    // --- Empty result ---

    test("getStillingsprosentListe returns empty list when client returns empty list") {
        val client = mockk<SPKStillingsprosentSoapClient> {
            every { getStillingsprosenter(fnr, any()) } returns emptyList()
        }

        val result = service(client).getStillingsprosentListe(fnr, tpOrdning())

        result shouldBe emptyList()
    }

    // --- EgressException ---

    test("getStillingsprosentListe returns empty list when client throws EgressException") {
        val client = mockk<SPKStillingsprosentSoapClient> {
            every { getStillingsprosenter(fnr, any()) } throws EgressException("SOAP error")
        }

        val result = service(client).getStillingsprosentListe(fnr, tpOrdning())

        result shouldBe emptyList()
    }
})
