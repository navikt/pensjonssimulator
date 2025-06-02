package client

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.client.ResponseFormatter

class ResponseFormatterTest : StringSpec({

    "json-formatting beholder tall etter komma" {

        val response = """
            {"grunnbeloepSats":1.0,"minstepensjonSats":"0.0"}
        """.trimIndent()

        val formattedResponse = ResponseFormatter.format(response)
        formattedResponse shouldBe response
    }
})
