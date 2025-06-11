package no.nav.pensjon.simulator.vedlikehold.web

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService

class VedlikeholdsmodusInterceptorTest : FunSpec({

    lateinit var featureToggleService: FeatureToggleService
    lateinit var request: HttpServletRequest
    lateinit var response: HttpServletResponse
    lateinit var interceptor: VedlikeholdsmodusInterceptor

    beforeTest {
        featureToggleService = mockk()
        request = mockk(relaxed = true)
        response = mockk(relaxed = true)
        interceptor = VedlikeholdsmodusInterceptor(featureToggleService)
    }

    test("naar vedlikeholdsmodus er aktivert skal forespørselen blokkeres") {
        every { featureToggleService.isEnabled("pensjonskalkulator.vedlikeholdsmodus") } returns true

        val result = interceptor.preHandle(request, response, "handler")

        result shouldBe false
        verify { response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Vedlikeholdsmodus er aktivert") }
    }

    test("naar vedlikeholdsmodus er deaktivert skal forespørselen tillates") {
        every { featureToggleService.isEnabled("pensjonskalkulator.vedlikeholdsmodus") } returns false

        val result = interceptor.preHandle(request, response, "handler")

        result shouldBe true
        verify(exactly = 0) { response.sendError(any(), any()) }
    }
})
