package no.nav.pensjon.simulator.vedlikehold.web

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService
import org.springframework.web.servlet.HandlerInterceptor

class VedlikeholdsmodusInterceptor(private val featureToggleService: FeatureToggleService) : HandlerInterceptor {
    val log = KotlinLogging.logger {}

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {

        if (featureToggleService.isEnabled("pensjonskalkulator.vedlikeholdsmodus")) {
            log.info { "Vedlikeholdsmodus er aktivert" }
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Vedlikeholdsmodus er aktivert")
            return false
        }
        return true
    }
}