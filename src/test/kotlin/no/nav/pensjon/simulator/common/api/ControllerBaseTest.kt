package no.nav.pensjon.simulator.common.api

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer
import no.nav.pensjon.simulator.generelt.organisasjon.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.statistikk.SimuleringHendelse
import no.nav.pensjon.simulator.statistikk.StatistikkService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.TilknytningService
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ControllerBaseTest : ShouldSpec({

    val pid = Pid("12906498357")

    context("registrerHendelse") {
        should("registrere simuleringstype 'ALDER' når organisasjonen ikke er Nav") {
            val statistikk = mockk<StatistikkService>(relaxed = true)

            val controller = TestController(
                traceAid = mockk(),
                statistikk,
                organisasjonsnummerProvider = arrangeOrganisasjonsnummer(nummer = "123456789"),
                tilknytningService = mockk()
            )

            controller.testRegistrerHendelse(SimuleringTypeEnum.ENDR_ALDER_M_GJEN)

            verify(exactly = 1) {
                statistikk.registrer(
                    SimuleringHendelse(
                        organisasjonsnummer = Organisasjonsnummer(value = "123456789"),
                        simuleringstype = SimuleringTypeEnum.ALDER
                    )
                )
            }
        }
    }

    context("verifiserAtBrukerTilknyttetTpLeverandoer") {
        should("ikke kaste exception når personen er tilknyttet tjenestepensjonsleverandør") {
            val controller = TestController(
                traceAid = mock(),
                organisasjonsnummerProvider = arrangeOrganisasjonsnummer(nummer = "123456789"),
                tilknytningService = arrangeErTilknyttet()
            )

            controller.testVerifiserAtBrukerTilknyttetTpLeverandoer(pid)
        }

        should("ikke kaste exception når Nav simulerer (i dev)") {
            val controller = TestController(
                traceAid = mockk(),
                organisasjonsnummerProvider = arrangeOrganisasjonsnummer(nummer = "889640782"),
                tilknytningService = mockk()
            )

            controller.testVerifiserAtBrukerTilknyttetTpLeverandoer(pid)
        }

        should("kaste exception med årsak når personen ikke er tilknyttet tjenestepensjonsleverandør") {
            val controller = TestController(
                traceAid = mockk<TraceAid>().apply { every { callId() } returns "123-124" },
                organisasjonsnummerProvider = mockk(relaxed = true),
                tilknytningService = mockk(relaxed = true)
            )

            val exception =
                shouldThrow<ResponseStatusException> { controller.testVerifiserAtBrukerTilknyttetTpLeverandoer(pid) }

            with(exception) {
                message shouldBe """403 FORBIDDEN "Call ID: 123-124 | Error: Brukeren er ikke tilknyttet angitt TP-leverandør""""
                reason shouldBe "Call ID: 123-124 | Error: Brukeren er ikke tilknyttet angitt TP-leverandør"
                statusCode shouldBe HttpStatus.FORBIDDEN
            }
        }
    }

    context("timed (no argument)") {
        should("returnere resultatet fra funksjonen") {
            val controller = SimpleTestController(mockk(relaxed = true))

            val result = controller.testTimed({ "result" }, "testFunction")

            result shouldBe "result"
        }

        should("kalle funksjonen nøyaktig én gang") {
            val controller = SimpleTestController(mockk(relaxed = true))
            var callCount = 0

            controller.testTimed({ callCount++ }, "testFunction")

            callCount shouldBe 1
        }

        should("håndtere funksjoner som returnerer null") {
            val controller = SimpleTestController(mockk(relaxed = true))

            val result = controller.testTimed<String?>({ null }, "testFunction")

            result shouldBe null
        }
    }

    context("timed (with argument)") {
        should("returnere resultatet fra funksjonen med argument") {
            val controller = SimpleTestController(mockk(relaxed = true))

            val result = controller.testTimedWithArg({ x: Int -> x * 2 }, 5, "testFunction")

            result shouldBe 10
        }

        should("sende argumentet til funksjonen") {
            val controller = SimpleTestController(mockk(relaxed = true))
            var receivedArg: String? = null

            controller.testTimedWithArg({ arg: String -> receivedArg = arg }, "testArg", "testFunction")

            receivedArg shouldBe "testArg"
        }
    }

    context("handle") {
        should("kaste INTERNAL_SERVER_ERROR for client error") {
            val traceAid = mockk<TraceAid>(relaxed = true)
            every { traceAid.callId() } returns "test-call-id"
            val controller = SimpleTestController(traceAid)
            val egressException = EgressException("Client error", statusCode = HttpStatus.BAD_REQUEST)

            val exception = shouldThrow<ResponseStatusException> {
                controller.testHandle<String>(egressException)
            }

            exception.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
            exception.reason shouldContain "test-call-id"
        }

        should("kaste SERVICE_UNAVAILABLE for server error") {
            val traceAid = mockk<TraceAid>(relaxed = true)
            every { traceAid.callId() } returns "test-call-id"
            val controller = SimpleTestController(traceAid)
            val egressException = EgressException("Server error", statusCode = HttpStatus.INTERNAL_SERVER_ERROR)

            val exception = shouldThrow<ResponseStatusException> {
                controller.testHandle<String>(egressException)
            }

            exception.statusCode shouldBe HttpStatus.SERVICE_UNAVAILABLE
        }

        should("kaste SERVICE_UNAVAILABLE for 5xx errors") {
            val traceAid = mockk<TraceAid>(relaxed = true)
            every { traceAid.callId() } returns "call-123"
            val controller = SimpleTestController(traceAid)
            val egressException = EgressException("Bad gateway", statusCode = HttpStatus.BAD_GATEWAY)

            val exception = shouldThrow<ResponseStatusException> {
                controller.testHandle<String>(egressException)
            }

            exception.statusCode shouldBe HttpStatus.SERVICE_UNAVAILABLE
        }
    }

    context("badRequest") {
        should("kaste BAD_REQUEST med call ID i melding") {
            val traceAid = mockk<TraceAid>(relaxed = true)
            every { traceAid.callId() } returns "bad-request-call-id"
            val controller = SimpleTestController(traceAid)
            val runtimeException = RuntimeException("Invalid input")

            val exception = shouldThrow<ResponseStatusException> {
                controller.testBadRequest<String>(runtimeException)
            }

            exception.statusCode shouldBe HttpStatus.BAD_REQUEST
            exception.reason shouldContain "bad-request-call-id"
            exception.reason shouldContain "Invalid input"
        }

        should("håndtere nestede exceptions") {
            val traceAid = mockk<TraceAid>(relaxed = true)
            every { traceAid.callId() } returns "call-id"
            val controller = SimpleTestController(traceAid)
            val cause = IllegalArgumentException("Root cause")
            val runtimeException = RuntimeException("Outer exception", cause)

            val exception = shouldThrow<ResponseStatusException> {
                controller.testBadRequest<String>(runtimeException)
            }

            exception.reason shouldContain "Outer exception"
            exception.reason shouldContain "Root cause"
        }
    }

    context("extractMessageRecursively") {
        should("returnere exception message") {
            val exception = RuntimeException("Simple error")

            val result = SimpleTestController.testExtractMessageRecursively(exception)

            result shouldBe "Simple error"
        }

        should("inkludere cause message") {
            val cause = IllegalArgumentException("Root cause")
            val exception = RuntimeException("Outer error", cause)

            val result = SimpleTestController.testExtractMessageRecursively(exception)

            result shouldContain "Outer error"
            result shouldContain "Cause:"
            result shouldContain "Root cause"
        }

        should("håndtere flere nivåer av causes") {
            val rootCause = IllegalStateException("Root")
            val middleCause = IllegalArgumentException("Middle", rootCause)
            val exception = RuntimeException("Top", middleCause)

            val result = SimpleTestController.testExtractMessageRecursively(exception)

            result shouldContain "Top"
            result shouldContain "Middle"
            result shouldContain "Root"
        }

        should("bruke class name når message er null") {
            val exception = RuntimeException()

            val result = SimpleTestController.testExtractMessageRecursively(exception)

            result shouldContain "RuntimeException"
        }
    }
})

/**
 * NB: Since Organisasjonsnummer is a @JvmInline value class, Mockk does not support it. Using Mockito instead.
 */
private fun arrangeOrganisasjonsnummer(nummer: String): OrganisasjonsnummerProvider =
    mock<OrganisasjonsnummerProvider>().apply {
        whenever(provideOrganisasjonsnummer()).thenReturn(Organisasjonsnummer(nummer))
    }

/**
 * NB: Since arrangeOrganisasjonsnummer uses Mockito for OrganisasjonsnummerProvider,
 * have to use Mockito for TilknytningService if they are mocked in the same test.
 */
private fun arrangeErTilknyttet(): TilknytningService =
    mock<TilknytningService>().apply {
        whenever(erPersonTilknyttetTjenestepensjonsordning(any(), any())).thenReturn(true)
    }

class TestController(
    traceAid: TraceAid,
    statistikk: StatistikkService? = null,
    organisasjonsnummerProvider: OrganisasjonsnummerProvider,
    tilknytningService: TilknytningService
) :
    ControllerBase(traceAid, statistikk, organisasjonsnummerProvider, tilknytningService) {

    fun testVerifiserAtBrukerTilknyttetTpLeverandoer(pid: Pid) =
        verifiserAtBrukerTilknyttetTpLeverandoer(pid)

    fun testRegistrerHendelse(simuleringstype: SimuleringTypeEnum) {
        registrerHendelse(simuleringstype)
    }

    override fun errorMessage(): String = "error from TestController"
}

/**
 * Simple test controller using the single-argument constructor for simpler tests.
 */
private class SimpleTestController(traceAid: TraceAid) : ControllerBase(traceAid) {
    override fun errorMessage(): String = "Test error"

    fun <R> testTimed(function: () -> R, functionName: String): R = timed(function, functionName)
    fun <A, R> testTimedWithArg(function: (A) -> R, argument: A, functionName: String): R =
        timed(function, argument, functionName)
    fun <T> testHandle(e: EgressException): T? = handle(e)
    fun <T> testBadRequest(e: RuntimeException): T = badRequest(e)

    companion object {
        fun testExtractMessageRecursively(e: Throwable): String = extractMessageRecursively(e)
    }
}
