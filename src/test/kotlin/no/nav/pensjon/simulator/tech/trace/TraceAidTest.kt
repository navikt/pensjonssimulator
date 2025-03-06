package no.nav.pensjon.simulator.tech.trace

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import jakarta.servlet.http.HttpServletRequest
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class TraceAidTest : FunSpec({

    test("'begin' should generate call ID if none supplied") {
        val callIdGenerator = mock(CallIdGenerator::class.java)
        `when`(callIdGenerator.newId()).thenReturn("generated")
        val traceAid = TraceAid(callIdGenerator)

        traceAid.begin() // none supplied

        traceAid.callId() shouldBe "generated"
    }

    test("'begin' should use supplied call ID") {
        val callIdGenerator = mock(CallIdGenerator::class.java)
        val request = mock(HttpServletRequest::class.java)
        `when`(request.getHeader("x-request-id")).thenReturn("supplied")
        val traceAid = TraceAid(callIdGenerator)

        traceAid.begin(request)

        traceAid.callId() shouldBe "supplied"
        verify(callIdGenerator, never()).newId()
    }
})
