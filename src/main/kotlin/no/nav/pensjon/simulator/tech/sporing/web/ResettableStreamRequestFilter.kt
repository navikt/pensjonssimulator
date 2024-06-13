package no.nav.pensjon.simulator.tech.sporing.web

import jakarta.servlet.*
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component

/**
 * Code adapted from https://github.com/glaudiston/spring-boot-rest-payload-logging
 */
@Component
class ResettableStreamRequestFilter : Filter {

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val wrappedResponse = ResettableStreamHttpServletResponse(response as HttpServletResponse)
        chain.doFilter(request, wrappedResponse)
    }
}
