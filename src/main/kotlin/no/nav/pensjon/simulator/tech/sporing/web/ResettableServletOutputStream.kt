package no.nav.pensjon.simulator.tech.sporing.web

import jakarta.servlet.ServletOutputStream
import jakarta.servlet.WriteListener
import java.io.OutputStream

/**
 * Code adapted from https://github.com/glaudiston/spring-boot-rest-payload-logging
 */
class ResettableServletOutputStream(
    private val response: ResettableStreamHttpServletResponse
) : ServletOutputStream() {
    val outputStream: OutputStream = response.getWrappedOutputStream()

    private val servletOutputStream: ServletOutputStream = object : ServletOutputStream() {
        var ready: Boolean = true
        var listener: WriteListener? = null

        override fun setWriteListener(listener: WriteListener) {
            this.listener = listener
        }

        override fun isReady(): Boolean = ready

        override fun write(b: Int) {
            outputStream.write(b)
            response.rawData.add(b.toByte())
        }
    }

    override fun close() {
        outputStream.close()
    }

    override fun setWriteListener(listener: WriteListener) {
        servletOutputStream.setWriteListener(listener)
    }

    override fun isReady(): Boolean = servletOutputStream.isReady

    override fun write(b: Int) {
        servletOutputStream.write(b)
    }
}
