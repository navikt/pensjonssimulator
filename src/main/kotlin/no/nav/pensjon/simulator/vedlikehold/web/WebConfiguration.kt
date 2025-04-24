package no.nav.pensjon.simulator.vedlikehold.web

import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
open class WebConfiguration(private val featureToggleService: FeatureToggleService) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(VedlikeholdsmodusInterceptor(featureToggleService))
            .addPathPatterns(
                "/api/*",
            )
    }
}