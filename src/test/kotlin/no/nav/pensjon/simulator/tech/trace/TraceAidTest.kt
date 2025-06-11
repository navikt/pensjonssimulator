package no.nav.pensjon.simulator.tech.trace

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest

class TraceAidTest : FunSpec({

    test("'begin' should generate call ID if none supplied") {
        val callIdGenerator = mockk<CallIdGenerator>().apply { every { newId() } returns "generated" }
        val traceAid = TraceAid(callIdGenerator)

        traceAid.begin() // none supplied

        traceAid.callId() shouldBe "generated"
    }

    test("'begin' should use supplied call ID") {
        val callIdGenerator = mockk<CallIdGenerator>()
        val request = mockk<HttpServletRequest>().apply {
            every { getHeader("Nav-Call-Id") } returns null
            every { getHeader("x-request-id") } returns "supplied"
        }
        val traceAid = TraceAid(callIdGenerator)

        traceAid.begin(request)

        traceAid.callId() shouldBe "supplied"
        verify { callIdGenerator wasNot Called }
    }
})
