package no.nav.pensjon.simulator.tech.sporing

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer
import no.nav.pensjon.simulator.generelt.organisasjon.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.sporing.client.SporingsloggClient
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class SporingsloggServiceTest : FunSpec({

    val pid = Pid("12345678910")
    val organisasjonsnummer = Organisasjonsnummer("123456789")

    fun mockClient(latch: CountDownLatch, sporingSlot: CapturingSlot<Sporing>) =
        mockk<SporingsloggClient> {
            every { log(capture(sporingSlot)) } answers { latch.countDown() }
        }

    fun mockProvider() =
        mockk<OrganisasjonsnummerProvider> {
            every { provideOrganisasjonsnummer() } returns organisasjonsnummer
        }

    // --- log ---

    test("log calls client with Sporing containing provided pid, dataForespoersel, and leverteData") {
        val latch = CountDownLatch(1)
        val sporingSlot = slot<Sporing>()
        val client = mockClient(latch, sporingSlot)

        SporingsloggService(client, mockProvider()).log(pid, "forespørsel", "data")

        latch.await(2, TimeUnit.SECONDS) shouldBe true
        sporingSlot.captured.pid shouldBe pid
        sporingSlot.captured.dataForespoersel shouldBe "forespørsel"
        sporingSlot.captured.leverteData shouldBe "data"
    }

    test("log fetches organisasjonsnummer from provider and uses it as mottaker") {
        val latch = CountDownLatch(1)
        val sporingSlot = slot<Sporing>()
        val client = mockClient(latch, sporingSlot)
        val provider = mockProvider()

        SporingsloggService(client, provider).log(pid, "forespoersel", "data")

        latch.await(2, TimeUnit.SECONDS) shouldBe true
        sporingSlot.captured.mottaker shouldBe organisasjonsnummer
        verify { provider.provideOrganisasjonsnummer() }
    }

    test("log sets tema to PEK and behandlingGrunnlag to B353") {
        val latch = CountDownLatch(1)
        val sporingSlot = slot<Sporing>()
        val client = mockClient(latch, sporingSlot)

        SporingsloggService(client, mockProvider()).log(pid, "f", "d")

        latch.await(2, TimeUnit.SECONDS) shouldBe true
        sporingSlot.captured.tema shouldBe "PEK"
        sporingSlot.captured.behandlingGrunnlag shouldBe "B353"
    }

    test("log sets uthentingTidspunkt to a non-null value") {
        val latch = CountDownLatch(1)
        val sporingSlot = slot<Sporing>()
        val client = mockClient(latch, sporingSlot)

        SporingsloggService(client, mockProvider()).log(pid, "f", "d")

        latch.await(2, TimeUnit.SECONDS) shouldBe true
        sporingSlot.captured.uthentingTidspunkt shouldNotBe null
    }

    // --- logUtgaaendeRequest ---

    test("logUtgaaendeRequest calls client with provided organisasjonsnummer as mottaker") {
        val latch = CountDownLatch(1)
        val sporingSlot = slot<Sporing>()
        val client = mockClient(latch, sporingSlot)
        val provider = mockProvider()

        SporingsloggService(client, provider).logUtgaaendeRequest(organisasjonsnummer, pid, "leverteData")

        latch.await(2, TimeUnit.SECONDS) shouldBe true
        sporingSlot.captured.mottaker shouldBe organisasjonsnummer
        verify(exactly = 0) { provider.provideOrganisasjonsnummer() }
    }

    test("logUtgaaendeRequest sets dataForespoersel to empty string") {
        val latch = CountDownLatch(1)
        val sporingSlot = slot<Sporing>()
        val client = mockClient(latch, sporingSlot)

        SporingsloggService(client, mockProvider()).logUtgaaendeRequest(organisasjonsnummer, pid, "leverteData")

        latch.await(2, TimeUnit.SECONDS) shouldBe true
        sporingSlot.captured.dataForespoersel shouldBe ""
    }

    test("logUtgaaendeRequest passes pid and leverteData to Sporing") {
        val latch = CountDownLatch(1)
        val sporingSlot = slot<Sporing>()
        val client = mockClient(latch, sporingSlot)

        SporingsloggService(client, mockProvider()).logUtgaaendeRequest(organisasjonsnummer, pid, "leverteData")

        latch.await(2, TimeUnit.SECONDS) shouldBe true
        sporingSlot.captured.pid shouldBe pid
        sporingSlot.captured.leverteData shouldBe "leverteData"
    }

    test("logUtgaaendeRequest sets tema to PEK and behandlingGrunnlag to B353") {
        val latch = CountDownLatch(1)
        val sporingSlot = slot<Sporing>()
        val client = mockClient(latch, sporingSlot)

        SporingsloggService(client, mockProvider()).logUtgaaendeRequest(organisasjonsnummer, pid, "data")

        latch.await(2, TimeUnit.SECONDS) shouldBe true
        sporingSlot.captured.tema shouldBe "PEK"
        sporingSlot.captured.behandlingGrunnlag shouldBe "B353"
    }
})
