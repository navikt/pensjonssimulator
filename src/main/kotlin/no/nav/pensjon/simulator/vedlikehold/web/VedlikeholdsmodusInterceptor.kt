package no.nav.pensjon.simulator.vedlikehold.web

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService
import org.springframework.web.servlet.HandlerInterceptor

class VedlikeholdsmodusInterceptor(private val featureToggleService: FeatureToggleService) : HandlerInterceptor {
    val log = KotlinLogging.logger {}

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {

        log.info { "Interceptor: Vedlikeholdsmodus er aktivert: " + featureToggleService.isEnabled("pensjonskalkulator.vedlikeholdsmodus") }
        return true
    }
}