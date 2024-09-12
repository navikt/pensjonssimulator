package no.nav.pensjon.simulator.tech.sporing.web

import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream
import java.io.IOException
import java.io.InputStream

/**
 * Adaptation of
 * github.com/glaudiston/spring-boot-rest-payload-logging/blob/master/src/main/java/com/example/restservice/util/ResettableServletInputStream.java
 */
class ResettableServletInputStream : ServletInputStream() {

    var inputStream: InputStream? = null

    private val servletInputStream: ServletInputStream = object : ServletInputStream() {
        var finished: Boolean = false
        var ready: Boolean = true
        var listener: ReadListener? = null

        @Throws(IOException::class)
        override fun read(): Int {
            val byte = inputStream!!.read()
            finished = byte == -1
            ready = !finished
            return byte
        }

        override fun isFinished(): Boolean = finished

        override fun isReady(): Boolean = ready

        override fun setReadListener(listener: ReadListener) {
            this.listener = listener
        }
    }

    @Throws(IOException::class)
    override fun available(): Int = inputStream!!.available()

    @Throws(IOException::class)
    override fun close() {
        inputStream!!.close()
    }

    override fun mark(readLimit: Int) {
        inputStream!!.mark(readLimit)
    }

    override fun markSupported(): Boolean = inputStream!!.markSupported()

    @Throws(IOException::class)
    override fun read(b: ByteArray, off: Int, len: Int): Int = inputStream!!.read(b, off, len)

    @Throws(IOException::class)
    fun readline(bytes: ByteArray, offset: Int, length: Int): Int {
        if (length <= 0) {
            return 0
        }

        var count = 0
        var value: Int
        var index = offset

        while ((read().also { value = it }) != -1) {
            bytes[index++] = value.toByte()
            count++

            if (value == '\n'.code || count == length) {
                break
            }
        }

        return if (count > 0) count else -1
    }

    @Throws(IOException::class)
    override fun reset() {
        inputStream!!.reset()
    }

    @Throws(IOException::class)
    override fun skip(n: Long): Long = inputStream!!.skip(n)

    @Throws(IOException::class)
    override fun read(): Int = inputStream!!.read()

    override fun setReadListener(readListener: ReadListener?) {
        servletInputStream.setReadListener(readListener)
    }

    override fun isReady(): Boolean = servletInputStream.isReady

    override fun isFinished(): Boolean = servletInputStream.isFinished
}
