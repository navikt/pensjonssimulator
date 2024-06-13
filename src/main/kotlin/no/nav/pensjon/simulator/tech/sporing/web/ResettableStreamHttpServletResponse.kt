package no.nav.pensjon.simulator.tech.sporing.web

import jakarta.servlet.ServletOutputStream
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpServletResponseWrapper
import java.io.OutputStreamWriter
import java.io.PrintWriter

/**
 * Code adapted from https://github.com/glaudiston/spring-boot-rest-payload-logging
 */
class ResettableStreamHttpServletResponse(
    val response: HttpServletResponse
) : HttpServletResponseWrapper(response) {
    val rawData: MutableList<Byte> = mutableListOf()
    private val resettableOutputStream = ResettableServletOutputStream(response = this)

    override fun getOutputStream(): ServletOutputStream = resettableOutputStream

    fun getWrappedOutputStream(): ServletOutputStream = response.outputStream

    override fun getWriter(): PrintWriter =
        characterEncoding
            ?.let { PrintWriter(OutputStreamWriter(resettableOutputStream, it)) }
            ?: PrintWriter(OutputStreamWriter(resettableOutputStream))
}
