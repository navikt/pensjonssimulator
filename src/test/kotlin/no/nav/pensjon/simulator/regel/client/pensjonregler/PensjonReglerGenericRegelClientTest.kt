package no.nav.pensjon.simulator.regel.client.pensjonregler

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tech.web.WebClientBase

class PensjonReglerGenericRegelClientTest : ShouldSpec({

    context("EgressException thrown") {
        should("rethrow exception") {
            val exception = EgressException("feil")

            shouldThrow<EgressException> {
                PensjonReglerGenericRegelClient(
                    baseUrl = "",
                    retryAttempts = "0",
                    webClientBase = arrangeWebClient(exception),
                    objectMapper = mockk(),
                    traceAid = mockk()
                ).makeRegelCall(
                    request = Any(),
                    responseClass = String::class.java,
                    serviceName = ""
                )
            } shouldBe exception
        }
    }

    context("other exception type than EgressException thrown") {
        should("throw EgressException with original exception as cause") {
            val originalException = IllegalStateException("feil")

            val exception = shouldThrow<EgressException> {
                PensjonReglerGenericRegelClient(
                    baseUrl = "",
                    retryAttempts = "0",
                    webClientBase = arrangeWebClient(originalException),
                    objectMapper = mockk(),
                    traceAid = mockk()
                ).makeRegelCall(
                    request = Any(),
                    responseClass = String::class.java,
                    serviceName = ""
                )
            }

            with(exception) {
                message shouldBe "Request failed to pensjon-regler"
                cause shouldBe originalException
            }
        }
    }
})

private fun arrangeWebClient(e: Exception): WebClientBase =
    mockk { every { withBaseUrl(any()) } returns mockk { every { post() } throws e } }