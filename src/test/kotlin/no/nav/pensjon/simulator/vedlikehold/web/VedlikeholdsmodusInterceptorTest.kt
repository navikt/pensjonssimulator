package no.nav.pensjon.simulator.vedlikehold.web

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VedlikeholdsmodusInterceptorTest {

    private lateinit var featureToggleService: FeatureToggleService
    private lateinit var request: HttpServletRequest
    private lateinit var response: HttpServletResponse
    private lateinit var interceptor: VedlikeholdsmodusInterceptor

    @BeforeEach
    fun setup() {
        featureToggleService = mockk()
        request = mockk(relaxed = true)
        response = mockk(relaxed = true)
        interceptor = VedlikeholdsmodusInterceptor(featureToggleService)
    }

    @Test
    fun `naar vedlikeholdsmodus er aktivert skal foresporselen blokkeres`() {
        every { featureToggleService.isEnabled("pensjonskalkulator.vedlikeholdsmodus") } returns true

        val result = interceptor.preHandle(request, response, "handler")

        result shouldBe false
        verify { response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Vedlikeholdsmodus er aktivert") }
    }

    @Test
    fun `naar vedlikeholdsmodus er deaktivert skal foresporselen tillates`() {
        every { featureToggleService.isEnabled("pensjonskalkulator.vedlikeholdsmodus") } returns false

        val result = interceptor.preHandle(request, response, "handler")

        result shouldBe true
        verify(exactly = 0) { response.sendError(any(), any()) }
    }
}