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
            when {
                request is ResettableStreamHttpServletRequest ->
                    if (response is ResettableStreamHttpServletResponse)
                        sendRequestAndResponseToSporingslogg(it, request, response)
                    else
                        sendRequestToSporingslogg(it, request)

                response is ResettableStreamHttpServletResponse -> sendResponseToSporingslogg(it, response)
                else -> log.warn { "No ResettableStream" }
            }
        } ?: log.warn { "ingen sporingslogging siden PID er udefinert" }
    }

    private fun sendRequestAndResponseToSporingslogg(
        pid: Pid,
        request: ResettableStreamHttpServletRequest,
        response: ResettableStreamHttpServletResponse
    ) {
        val inData = copy(request.rawData)
        val outData = copy(response.rawData)

        if (inData.isNotEmpty() || outData.isNotEmpty()) {
            service.log(pid, "In:${String(inData)}Out:${String(outData)}")
        }
    }

    private fun copy(bytes: ByteArray) =
        ByteArray(bytes.size).apply {
            this.indices.forEach { this[it] = bytes[it] }
        }

    private fun copy(bytes: MutableList<Byte>) =
        ByteArray(bytes.size).apply {
            this.indices.forEach { this[it] = bytes[it] }
        }

    private fun sendRequestToSporingslogg(pid: Pid, request: ResettableStreamHttpServletRequest) {
        val data = copy(request.rawData)

        if (data.isNotEmpty()) {
            service.log(pid, "In:${String(data)}")
        }
    }

    private fun sendResponseToSporingslogg(pid: Pid, response: ResettableStreamHttpServletResponse) {
        val data = copy(response.rawData)

        if (data.isNotEmpty()) {
            service.log(pid, "Out:${String(data)}")
        }
    }
}
