package no.nav.pensjon.simulator.tech.selftest.api.acl

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.selftest.PingResult
import no.nav.pensjon.simulator.tech.selftest.ServiceStatus
import java.time.LocalTime

class SelfTestResultV1Test : ShouldSpec({

    context("funksjonen 'transferable'") {
        should("angi applikasjonsnavn og tidspunkt") {
            val result = SelfTestResultV1.transferable(resultsByService = emptyMap(), time)

            with(result) {
                application shouldBe "pensjonssimulator"
                timestamp shouldBe "03:04:05"
            }
        }

        context("tom tjenesteliste") {
            should("returnere tom sjekkliste og aggregert resultat 0 (dvs. 'oppe')") {
                val result = SelfTestResultV1.transferable(resultsByService = emptyMap(), time)

                with(result) {
                    checks.shouldBeEmpty()
                    aggregateResult shouldBe 0
                }
            }
        }

        context("alle tjenester er oppe") {
            should("gi aggregert resultat 0 (dvs. 'oppe') og ingen feilmelding") {
                val pingResults = mapOf(
                    "pen" to PingResult(
                        service = EgressService.PENSJONSFAGLIG_KJERNE,
                        status = ServiceStatus.UP,
                        endpoint = "https://pen.nav.no/api",
                        message = "OK"
                    ),
                    "popp" to PingResult(
                        service = EgressService.PENSJONSOPPTJENING,
                        status = ServiceStatus.UP,
                        endpoint = "https://popp.nav.no/api",
                        message = "OK"
                    )
                )

                SelfTestResultV1.transferable(pingResults, time) shouldBe SelfTestResultV1(
                    application = "pensjonssimulator",
                    timestamp = "03:04:05",
                    aggregateResult = 0,
                    checks = listOf(
                        CheckResultV1(
                            endpoint = "https://pen.nav.no/api",
                            description = "Pensjonsfaglig kjerne",
                            errorMessage = null,
                            result = 0
                        ),
                        CheckResultV1(
                            endpoint = "https://popp.nav.no/api",
                            description = "Pensjonsopptjening",
                            errorMessage = null,
                            result = 0
                        )
                    )
                )
            }
        }

        context("minst én tjeneste er nede") {
            should("gi aggregert resultat 1 (dvs. 'nede') og angi feilmelding") {
                val pingResults = mapOf(
                    "service1" to createPingResult(ServiceStatus.UP),
                    "service2" to createPingResult(ServiceStatus.DOWN)
                )

                val result = SelfTestResultV1.transferable(pingResults, time)

                with(result) {
                    aggregateResult shouldBe 1
                    checks[0].result shouldBe 0 // oppe
                    checks[1] shouldBe CheckResultV1(
                        endpoint = "https://test.nav.no/api",
                        description = "Pensjonsfaglig kjerne",
                        errorMessage = "Error",
                        result = 1 // nede
                    )
                }
            }
        }
    }
})

private val time = LocalTime.of(3, 4, 5)

private fun createPingResult(
    status: ServiceStatus,
    endpoint: String = "https://test.nav.no/api"
) = PingResult(
    service = EgressService.PENSJONSFAGLIG_KJERNE,
    status,
    endpoint,
    message = if (status == ServiceStatus.UP) "OK" else "Error"
)