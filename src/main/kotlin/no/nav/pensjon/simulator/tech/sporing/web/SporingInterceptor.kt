package no.nav.pensjon.simulator.tech.sporing.web

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.sporing.SporingsloggService
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class SporingInterceptor(private val service: SporingsloggService) : HandlerInterceptor {

    private val log = KotlinLogging.logger {}

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        val pid: Pid? = request.getAttribute("pid") as? Pid

        pid?.let {
            if (response is ResettableStreamHttpServletResponse) {
                sendResponseToSporingslogg(it, response)
            }
        } ?: log.warn { "ingen sporingslogging siden PID er udefinert" }
    }

    private fun sendResponseToSporingslogg(pid: Pid, response: ResettableStreamHttpServletResponse) {
        val data = ByteArray(response.rawData.size)
        data.indices.forEach { data[it] = response.rawData[it] }

        if (data.isNotEmpty()) {
            service.log(pid, String(data))
        }
    }
}
