package no.nav.pensjon.simulator.testutil

import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.alderspensjon.spec.OffentligSimuleringstypeDeducer
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.tech.security.egress.EnrichedAuthentication
import no.nav.pensjon.simulator.tech.security.egress.config.EgressTokenSuppliersByService
import no.nav.pensjon.simulator.testutil.TestObjects.jwt
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDate

object Arrange {

    fun foedselsdato(year: Int, month: Int, dayOfMonth: Int): GeneralPersonService =
        mockk<GeneralPersonService>().apply {
            every { foedselsdato(pid) } returns LocalDate.of(year, month, dayOfMonth)
        }

    fun simuleringstype(
        type: SimuleringTypeEnum,
        uttakFom: LocalDate,
        livsvarigOffentligAfpRettFom: LocalDate?
    ): OffentligSimuleringstypeDeducer =
        mockk<OffentligSimuleringstypeDeducer>().apply {
            every { deduceSimuleringstype(pid, uttakFom, livsvarigOffentligAfpRettFom) } returns type
        }

    fun normalder(foedselsdato: LocalDate): NormertPensjonsalderService =
        mockk<NormertPensjonsalderService>().apply {
            every { normalder(foedselsdato) } returns Alder(67, 0)
        }

    fun normalder(): NormertPensjonsalderService =
        mockk<NormertPensjonsalderService>().apply {
            every { normalder(pid) } returns Alder(67, 0)
        }

    fun security() {
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext())

        SecurityContextHolder.getContext().authentication = EnrichedAuthentication(
            initialAuth = TestingAuthenticationToken("TEST_USER", jwt),
            egressTokenSuppliersByService = EgressTokenSuppliersByService(mapOf())
        )
    }

    fun webClientContextRunner(): ApplicationContextRunner =
        ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(WebClientTestConfig::class.java)
        )
}

fun MockWebServer.arrangeOkJsonResponse(body: String) {
    this.enqueue(
        MockResponse()
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setResponseCode(HttpStatus.OK.value()).setBody(body)
    )
}

fun MockWebServer.arrangeResponse(status: HttpStatus, body: String) {
    this.enqueue(MockResponse().setResponseCode(status.value()).setBody(body))
}
