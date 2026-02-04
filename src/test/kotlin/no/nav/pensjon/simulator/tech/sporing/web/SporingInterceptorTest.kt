package no.nav.pensjon.simulator.tech.sporing.web

import io.kotest.core.spec.style.FunSpec
import io.mockk.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.sporing.SporingsloggService

class SporingInterceptorTest : FunSpec({

    lateinit var service: SporingsloggService
    lateinit var interceptor: SporingInterceptor

    beforeTest {
        service = mockk(relaxed = true)
        interceptor = SporingInterceptor(service)
    }

    context("afterCompletion") {

        test("logger request og response når begge er ResettableStream og PID er satt") {
            val pid = Pid("12345678901")
            val requestData = "request body data"
            val responseData = "response body data"

            val innerRequest = mockk<HttpServletRequest>(relaxed = true)
            val innerResponse = mockk<HttpServletResponse>(relaxed = true)

            val request = mockk<ResettableStreamHttpServletRequest>(relaxed = true) {
                every { getAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME) } returns pid
                every { rawData } returns requestData.toByteArray()
                every { this@mockk.request } returns innerRequest
            }

            val response = mockk<ResettableStreamHttpServletResponse>(relaxed = true) {
                every { rawData } returns responseData.toByteArray().toMutableList()
                every { this@mockk.response } returns innerResponse
            }

            interceptor.afterCompletion(request, response, Any(), null)

            verify { service.log(pid, requestData, responseData) }
        }

        test("bruker requestURI når request rawData er tom") {
            val pid = Pid("12345678901")
            val requestUri = "/api/v1/test"
            val responseData = "response body data"

            val innerRequest = mockk<HttpServletRequest>(relaxed = true) {
                every { requestURI } returns requestUri
            }
            val innerResponse = mockk<HttpServletResponse>(relaxed = true)

            val request = mockk<ResettableStreamHttpServletRequest>(relaxed = true) {
                every { getAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME) } returns pid
                every { rawData } returns byteArrayOf()
                every { this@mockk.request } returns innerRequest
            }

            val response = mockk<ResettableStreamHttpServletResponse>(relaxed = true) {
                every { rawData } returns responseData.toByteArray().toMutableList()
                every { this@mockk.response } returns innerResponse
            }

            interceptor.afterCompletion(request, response, Any(), null)

            verify { service.log(pid, requestUri, responseData) }
        }

        test("bruker response status når response rawData er tom") {
            val pid = Pid("12345678901")
            val requestData = "request body data"
            val responseStatus = 200

            val innerRequest = mockk<HttpServletRequest>(relaxed = true)
            val innerResponse = mockk<HttpServletResponse>(relaxed = true) {
                every { status } returns responseStatus
            }

            val request = mockk<ResettableStreamHttpServletRequest>(relaxed = true) {
                every { getAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME) } returns pid
                every { rawData } returns requestData.toByteArray()
                every { this@mockk.request } returns innerRequest
            }

            val response = mockk<ResettableStreamHttpServletResponse>(relaxed = true) {
                every { rawData } returns mutableListOf()
                every { this@mockk.response } returns innerResponse
            }

            interceptor.afterCompletion(request, response, Any(), null)

            verify { service.log(pid, requestData, "200") }
        }

        test("bruker requestURI og status når begge rawData er tomme") {
            val pid = Pid("12345678901")
            val requestUri = "/api/v1/endpoint"
            val responseStatus = 404

            val innerRequest = mockk<HttpServletRequest>(relaxed = true) {
                every { requestURI } returns requestUri
            }
            val innerResponse = mockk<HttpServletResponse>(relaxed = true) {
                every { status } returns responseStatus
            }

            val request = mockk<ResettableStreamHttpServletRequest>(relaxed = true) {
                every { getAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME) } returns pid
                every { rawData } returns byteArrayOf()
                every { this@mockk.request } returns innerRequest
            }

            val response = mockk<ResettableStreamHttpServletResponse>(relaxed = true) {
                every { rawData } returns mutableListOf()
                every { this@mockk.response } returns innerResponse
            }

            interceptor.afterCompletion(request, response, Any(), null)

            verify { service.log(pid, requestUri, "404") }
        }

        test("logger kun request når response ikke er ResettableStream") {
            val pid = Pid("12345678901")
            val requestData = "request only"

            val innerRequest = mockk<HttpServletRequest>(relaxed = true)

            val request = mockk<ResettableStreamHttpServletRequest>(relaxed = true) {
                every { getAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME) } returns pid
                every { rawData } returns requestData.toByteArray()
                every { this@mockk.request } returns innerRequest
            }

            val response = mockk<HttpServletResponse>(relaxed = true)

            interceptor.afterCompletion(request, response, Any(), null)

            verify { service.log(pid, requestData, "(no data)") }
        }

        test("logger ikke request når request rawData er tom og response ikke er ResettableStream") {
            val pid = Pid("12345678901")

            val innerRequest = mockk<HttpServletRequest>(relaxed = true)

            val request = mockk<ResettableStreamHttpServletRequest>(relaxed = true) {
                every { getAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME) } returns pid
                every { rawData } returns byteArrayOf()
                every { this@mockk.request } returns innerRequest
            }

            val response = mockk<HttpServletResponse>(relaxed = true)

            interceptor.afterCompletion(request, response, Any(), null)

            verify(exactly = 0) { service.log(any(), any(), any()) }
        }

        test("logger kun response når request ikke er ResettableStream men response er det") {
            val pid = Pid("12345678901")
            val responseData = "response only"

            val request = mockk<HttpServletRequest>(relaxed = true) {
                every { getAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME) } returns pid
            }

            val innerResponse = mockk<HttpServletResponse>(relaxed = true)

            val response = mockk<ResettableStreamHttpServletResponse>(relaxed = true) {
                every { rawData } returns responseData.toByteArray().toMutableList()
                every { this@mockk.response } returns innerResponse
            }

            interceptor.afterCompletion(request, response, Any(), null)

            verify { service.log(pid, "(no data)", responseData) }
        }

        test("logger ikke response når response rawData er tom og request ikke er ResettableStream") {
            val pid = Pid("12345678901")

            val request = mockk<HttpServletRequest>(relaxed = true) {
                every { getAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME) } returns pid
            }

            val innerResponse = mockk<HttpServletResponse>(relaxed = true)

            val response = mockk<ResettableStreamHttpServletResponse>(relaxed = true) {
                every { rawData } returns mutableListOf()
                every { this@mockk.response } returns innerResponse
            }

            interceptor.afterCompletion(request, response, Any(), null)

            verify(exactly = 0) { service.log(any(), any(), any()) }
        }

        test("logger ikke når PID ikke er satt") {
            val request = mockk<HttpServletRequest>(relaxed = true) {
                every { getAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME) } returns null
                every { requestURI } returns "/api/test"
            }

            val response = mockk<HttpServletResponse>(relaxed = true)

            interceptor.afterCompletion(request, response, Any(), null)

            verify(exactly = 0) { service.log(any(), any(), any()) }
        }

        test("logger ikke når verken request eller response er ResettableStream") {
            val pid = Pid("12345678901")

            val request = mockk<HttpServletRequest>(relaxed = true) {
                every { getAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME) } returns pid
            }

            val response = mockk<HttpServletResponse>(relaxed = true)

            interceptor.afterCompletion(request, response, Any(), null)

            verify(exactly = 0) { service.log(any(), any(), any()) }
        }

        test("håndterer exception parameter uten å feile") {
            val pid = Pid("12345678901")
            val requestData = "request data"
            val responseData = "response data"

            val innerRequest = mockk<HttpServletRequest>(relaxed = true)
            val innerResponse = mockk<HttpServletResponse>(relaxed = true)

            val request = mockk<ResettableStreamHttpServletRequest>(relaxed = true) {
                every { getAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME) } returns pid
                every { rawData } returns requestData.toByteArray()
                every { this@mockk.request } returns innerRequest
            }

            val response = mockk<ResettableStreamHttpServletResponse>(relaxed = true) {
                every { rawData } returns responseData.toByteArray().toMutableList()
                every { this@mockk.response } returns innerResponse
            }

            val exception = RuntimeException("Test exception")

            interceptor.afterCompletion(request, response, Any(), exception)

            verify { service.log(pid, requestData, responseData) }
        }

        test("håndterer special characters i request og response data") {
            val pid = Pid("12345678901")
            val requestData = """{"name": "Æøå", "value": "特殊字符"}"""
            val responseData = """{"status": "ÆØÅ æøå"}"""

            val innerRequest = mockk<HttpServletRequest>(relaxed = true)
            val innerResponse = mockk<HttpServletResponse>(relaxed = true)

            val request = mockk<ResettableStreamHttpServletRequest>(relaxed = true) {
                every { getAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME) } returns pid
                every { rawData } returns requestData.toByteArray()
                every { this@mockk.request } returns innerRequest
            }

            val response = mockk<ResettableStreamHttpServletResponse>(relaxed = true) {
                every { rawData } returns responseData.toByteArray().toMutableList()
                every { this@mockk.response } returns innerResponse
            }

            interceptor.afterCompletion(request, response, Any(), null)

            verify { service.log(pid, requestData, responseData) }
        }

        test("håndterer store datamengder") {
            val pid = Pid("12345678901")
            val largeRequestData = "x".repeat(10000)
            val largeResponseData = "y".repeat(10000)

            val innerRequest = mockk<HttpServletRequest>(relaxed = true)
            val innerResponse = mockk<HttpServletResponse>(relaxed = true)

            val request = mockk<ResettableStreamHttpServletRequest>(relaxed = true) {
                every { getAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME) } returns pid
                every { rawData } returns largeRequestData.toByteArray()
                every { this@mockk.request } returns innerRequest
            }

            val response = mockk<ResettableStreamHttpServletResponse>(relaxed = true) {
                every { rawData } returns largeResponseData.toByteArray().toMutableList()
                every { this@mockk.response } returns innerResponse
            }

            interceptor.afterCompletion(request, response, Any(), null)

            verify { service.log(pid, largeRequestData, largeResponseData) }
        }

        test("bruker ulike response statuskoder") {
            val statuses = listOf(200, 201, 400, 401, 403, 404, 500, 502, 503)

            statuses.forEach { status ->
                val pid = Pid("12345678901")

                val innerRequest = mockk<HttpServletRequest>(relaxed = true)
                val innerResponse = mockk<HttpServletResponse>(relaxed = true) {
                    every { this@mockk.status } returns status
                }

                val request = mockk<ResettableStreamHttpServletRequest>(relaxed = true) {
                    every { getAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME) } returns pid
                    every { rawData } returns byteArrayOf()
                    every { this@mockk.request } returns innerRequest
                }

                val response = mockk<ResettableStreamHttpServletResponse>(relaxed = true) {
                    every { rawData } returns mutableListOf()
                    every { this@mockk.response } returns innerResponse
                }

                interceptor.afterCompletion(request, response, Any(), null)

                verify { service.log(pid, any(), status.toString()) }
                clearMocks(service)
            }
        }

        test("håndterer ulike PIDs") {
            val pids = listOf("12345678901", "98765432109", "11111111111")

            pids.forEach { pidValue ->
                val pid = Pid(pidValue)
                val requestData = "data for $pidValue"
                val responseData = "response for $pidValue"

                val innerRequest = mockk<HttpServletRequest>(relaxed = true)
                val innerResponse = mockk<HttpServletResponse>(relaxed = true)

                val request = mockk<ResettableStreamHttpServletRequest>(relaxed = true) {
                    every { getAttribute(SporingInterceptor.PID_ATTRIBUTE_NAME) } returns pid
                    every { rawData } returns requestData.toByteArray()
                    every { this@mockk.request } returns innerRequest
                }

                val response = mockk<ResettableStreamHttpServletResponse>(relaxed = true) {
                    every { rawData } returns responseData.toByteArray().toMutableList()
                    every { this@mockk.response } returns innerResponse
                }

                interceptor.afterCompletion(request, response, Any(), null)

                verify { service.log(pid, requestData, responseData) }
                clearMocks(service)
            }
        }
    }

    context("PID_ATTRIBUTE_NAME") {

        test("har korrekt verdi") {
            SporingInterceptor.PID_ATTRIBUTE_NAME shouldBe "pid"
        }
    }
})

private infix fun String.shouldBe(expected: String) {
    if (this != expected) throw AssertionError("Expected '$expected' but was '$this'")
}
