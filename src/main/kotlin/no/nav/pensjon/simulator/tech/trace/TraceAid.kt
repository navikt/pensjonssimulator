package no.nav.pensjon.simulator.tech.trace

import jakarta.servlet.http.HttpServletRequest
import no.nav.pensjon.simulator.tech.web.CustomHttpHeaders
import org.slf4j.MDC
import org.springframework.stereotype.Component

@Component
class TraceAid(private val callIdGenerator: CallIdGenerator) {

    fun begin(request: HttpServletRequest? = null) {
        val suppliedRequestId = request?.getHeader(CustomHttpHeaders.CALL_ID)
            ?: request?.getHeader(CustomHttpHeaders.EXTERNAL_REQUEST_ID)
        MDC.put(CALL_ID_KEY, suppliedRequestId ?: callIdGenerator.newId())
    }

    fun callId(): String =
        MDC.get(CALL_ID_KEY) ?: callIdGenerator.newId().also { MDC.put(CALL_ID_KEY, it) }

    fun end() {
        MDC.clear()
    }

    private companion object {
        private const val CALL_ID_KEY = "Nav-Call-Id"
    }
}
