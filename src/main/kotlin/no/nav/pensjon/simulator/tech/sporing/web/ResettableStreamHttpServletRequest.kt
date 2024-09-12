package no.nav.pensjon.simulator.tech.sporing.web

import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStreamReader

/**
 * Adaptation of
 * github.com/glaudiston/spring-boot-rest-payload-logging/blob/master/src/main/java/com/example/restservice/util/ResettableStreamHttpServletRequest.java
 */
class ResettableStreamHttpServletRequest(val request: HttpServletRequest) : HttpServletRequestWrapper(request) {

    var rawData = byteArrayOf()
    private val servletStream: ResettableServletInputStream = ResettableServletInputStream()

    @Throws(IOException::class)
    fun resetInputStream() {
        initRawData()
        servletStream.inputStream = ByteArrayInputStream(rawData)
    }

    @Throws(IOException::class)
    private fun initRawData() {
        if (rawData.isEmpty()) {
            rawData = request.inputStream.readBytes()
        }

        servletStream.inputStream = ByteArrayInputStream(rawData)
    }

    @Throws(IOException::class)
    override fun getInputStream(): ServletInputStream {
        initRawData()
        return servletStream
    }

    @Throws(IOException::class)
    override fun getReader(): BufferedReader {
        initRawData()

        return characterEncoding
            ?.let { BufferedReader(InputStreamReader(servletStream, it)) }
            ?: BufferedReader(InputStreamReader(servletStream))
    }
}
