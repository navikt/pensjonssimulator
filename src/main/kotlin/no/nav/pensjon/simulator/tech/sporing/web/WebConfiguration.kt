package no.nav.pensjon.simulator.tech.sporing.web

import no.nav.pensjon.simulator.tech.sporing.SporingsloggService
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfiguration(val sporingsloggService: SporingsloggService) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(SporingInterceptor(sporingsloggService))
            .addPathPatterns("/api/v4/simuler-alderspensjon")
    }
}
