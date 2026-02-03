package no.nav.pensjon.simulator.tech.selftest.api.acl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.selftest.PingResult
import no.nav.pensjon.simulator.tech.selftest.SelfTest.Companion.APPLICATION_NAME
import no.nav.pensjon.simulator.tech.selftest.ServiceStatus

class SelfTestResultMapperV1Test : FunSpec({

    context("dto") {

        test("setter application til APPLICATION_NAME") {
            val pingResults = emptyMap<String, PingResult>()

            val result = SelfTestResultMapperV1.dto(pingResults)

            result.application shouldBe APPLICATION_NAME
            result.application shouldBe "pensjonssimulator"
        }

        test("setter timestamp") {
            val pingResults = emptyMap<String, PingResult>()

            val result = SelfTestResultMapperV1.dto(pingResults)

            result.timestamp shouldNotBe null
            result.timestamp.isNotEmpty() shouldBe true
        }

        test("returnerer tom checks liste for tom input") {
            val pingResults = emptyMap<String, PingResult>()

            val result = SelfTestResultMapperV1.dto(pingResults)

            result.checks.shouldBeEmpty()
        }

        test("aggregateResult er UP (0) når alle tjenester er UP") {
            val pingResults = mapOf(
                "service1" to createPingResult(ServiceStatus.UP),
                "service2" to createPingResult(ServiceStatus.UP),
                "service3" to createPingResult(ServiceStatus.UP)
            )

            val result = SelfTestResultMapperV1.dto(pingResults)

            result.aggregateResult shouldBe 0
        }

        test("aggregateResult er DOWN (1) når minst én tjeneste er DOWN") {
            val pingResults = mapOf(
                "service1" to createPingResult(ServiceStatus.UP),
                "service2" to createPingResult(ServiceStatus.DOWN),
                "service3" to createPingResult(ServiceStatus.UP)
            )

            val result = SelfTestResultMapperV1.dto(pingResults)

            result.aggregateResult shouldBe 1
        }

        test("aggregateResult er DOWN (1) når alle tjenester er DOWN") {
            val pingResults = mapOf(
                "service1" to createPingResult(ServiceStatus.DOWN),
                "service2" to createPingResult(ServiceStatus.DOWN)
            )

            val result = SelfTestResultMapperV1.dto(pingResults)

            result.aggregateResult shouldBe 1
        }

        test("aggregateResult er UP (0) for tom liste") {
            val pingResults = emptyMap<String, PingResult>()

            val result = SelfTestResultMapperV1.dto(pingResults)

            result.aggregateResult shouldBe 0
        }

        test("mapper enkelt PingResult til CheckResultV1") {
            val pingResult = PingResult(
                service = EgressService.PENSJONSFAGLIG_KJERNE,
                status = ServiceStatus.UP,
                endpoint = "https://pen.nav.no/api",
                message = "OK"
            )
            val pingResults = mapOf("pen" to pingResult)

            val result = SelfTestResultMapperV1.dto(pingResults)

            result.checks shouldHaveSize 1
            result.checks[0].endpoint shouldBe "https://pen.nav.no/api"
            result.checks[0].description shouldBe "Pensjonsfaglig kjerne"
            result.checks[0].result shouldBe 0
        }

        test("mapper flere PingResult til CheckResultV1") {
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

            val result = SelfTestResultMapperV1.dto(pingResults)

            result.checks shouldHaveSize 2
        }

        test("setter errorMessage til null for UP status") {
            val pingResult = PingResult(
                service = EgressService.PENSJONSFAGLIG_KJERNE,
                status = ServiceStatus.UP,
                endpoint = "https://pen.nav.no/api",
                message = "Service is running"
            )
            val pingResults = mapOf("pen" to pingResult)

            val result = SelfTestResultMapperV1.dto(pingResults)

            result.checks[0].errorMessage shouldBe null
        }

        test("setter errorMessage til message for DOWN status") {
            val pingResult = PingResult(
                service = EgressService.PENSJONSFAGLIG_KJERNE,
                status = ServiceStatus.DOWN,
                endpoint = "https://pen.nav.no/api",
                message = "Connection timeout"
            )
            val pingResults = mapOf("pen" to pingResult)

            val result = SelfTestResultMapperV1.dto(pingResults)

            result.checks[0].errorMessage shouldBe "Connection timeout"
        }

        test("result er 0 for UP status") {
            val pingResult = createPingResult(ServiceStatus.UP)
            val pingResults = mapOf("service" to pingResult)

            val result = SelfTestResultMapperV1.dto(pingResults)

            result.checks[0].result shouldBe 0
        }

        test("result er 1 for DOWN status") {
            val pingResult = createPingResult(ServiceStatus.DOWN)
            val pingResults = mapOf("service" to pingResult)

            val result = SelfTestResultMapperV1.dto(pingResults)

            result.checks[0].result shouldBe 1
        }

        test("henter description fra EgressService") {
            val services = listOf(
                EgressService.PENSJONSFAGLIG_KJERNE to "Pensjonsfaglig kjerne",
                EgressService.PENSJONSOPPTJENING to "Pensjonsopptjening",
                EgressService.PENSJON_REGLER to "Pensjonsfaglig regelmotor",
                EgressService.FSS_GATEWAY to "FSS-gateway"
            )

            services.forEach { (service, expectedDescription) ->
                val pingResult = PingResult(
                    service = service,
                    status = ServiceStatus.UP,
                    endpoint = "https://test.nav.no",
                    message = "OK"
                )
                val pingResults = mapOf("test" to pingResult)

                val result = SelfTestResultMapperV1.dto(pingResults)

                result.checks[0].description shouldBe expectedDescription
            }
        }

        test("håndterer blanding av UP og DOWN statuser") {
            val pingResults = mapOf(
                "up1" to createPingResult(ServiceStatus.UP, "endpoint1"),
                "down" to PingResult(
                    service = EgressService.PENSJONSFAGLIG_KJERNE,
                    status = ServiceStatus.DOWN,
                    endpoint = "endpoint2",
                    message = "Error message"
                ),
                "up2" to createPingResult(ServiceStatus.UP, "endpoint3")
            )

            val result = SelfTestResultMapperV1.dto(pingResults)

            result.aggregateResult shouldBe 1 // DOWN
            result.checks shouldHaveSize 3

            val downCheck = result.checks.first { it.result == 1 }
            downCheck.errorMessage shouldBe "Error message"

            val upChecks = result.checks.filter { it.result == 0 }
            upChecks shouldHaveSize 2
            upChecks.forEach { it.errorMessage shouldBe null }
        }
    }
})

private fun createPingResult(
    status: ServiceStatus,
    endpoint: String = "https://test.nav.no/api"
) = PingResult(
    service = EgressService.PENSJONSFAGLIG_KJERNE,
    status = status,
    endpoint = endpoint,
    message = if (status == ServiceStatus.UP) "OK" else "Error"
)
