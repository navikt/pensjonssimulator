package no.nav.pensjon.simulator.tech.selftest.api

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.every
import no.nav.pensjon.simulator.tech.security.egress.SecurityContextEnricher
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.selftest.PingResult
import no.nav.pensjon.simulator.tech.selftest.SelfTest
import no.nav.pensjon.simulator.tech.selftest.ServiceStatus
import no.nav.pensjon.simulator.tech.sporing.SporingsloggService
import no.nav.pensjon.simulator.tech.time.Tid
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService
import no.nav.pensjon.simulator.tech.trace.TraceAid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalTime

@WebMvcTest(SelfTestController::class)
class SelfTestControllerTest : ShouldSpec() {

    @Autowired
    private lateinit var mvc: MockMvc

    @MockkBean
    private lateinit var selfTest: SelfTest

    @MockkBean
    private lateinit var tid: Tid

    @MockkBean
    private lateinit var securityContextEnricher: SecurityContextEnricher

    @MockkBean(relaxed = true)
    private lateinit var traceAid: TraceAid

    @MockkBean
    private lateinit var sporingsloggService: SporingsloggService

    @MockkBean
    private lateinit var feature: FeatureToggleService

    init {
        beforeSpec {
            every { tid.naa() } returns LocalTime.of(10, 11, 12)
            every { securityContextEnricher.enrichAuthentication(any()) } returns Unit
            every { feature.isEnabled(any()) } returns false // vedlikeholdsmodus deaktivert
            every { sporingsloggService.log(any(), any(), any()) } returns Unit
            every { traceAid.begin(any()) } returns Unit
        }

        context("alt OK") {
            should("utføre selvtest og returnere OK resultat") {
                every { selfTest.perform() } returns mapOf(
                    "service" to PingResult(
                        service = EgressService.AFP_BEHOLDNING_API,
                        status = ServiceStatus.UP,
                        endpoint = "https://test.nav.no/api",
                        message = "OK"
                    )
                )
                mvc.perform(get(URL).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(
                        content().json(
                            """{
  "application" : "pensjonssimulator",
  "timestamp" : "10:11:12",
  "aggregateResult" : 0,
  "checks" : [ {
    "endpoint" : "https://test.nav.no/api",
    "description" : "AFP Beholdning API",
    "result" : 0
  } ]
}"""
                        )
                    )
            }
        }
    }
}

private const val URL = "/internal/selftest"