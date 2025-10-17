package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class TpUtilTest : ShouldSpec({

    context("redact") {
        should("maskere fødselsnummer") {
            TpUtil.redact("02345678901") shouldBe "***********"
        }

        should("maskere alle fødselsnummer i en tekst") {
            TpUtil.redact("ident:22345600001fnr:12345600001pid:01410199999") shouldBe "ident:***********fnr:***********pid:***********"
        }

        should("maskere fødselsnummer i en tekst med mellomrom") {
            TpUtil.redact("ident:22345600001 fnr:12345600001 pid:01410199999") shouldBe "ident:*********** fnr:*********** pid:***********"
        }

        should("endre ikke tekst med desimaltall") {
            val tekst = "ident:2234.5600001 fnr:1.2345600001 pid:0141019999.9"
            TpUtil.redact(tekst) shouldBe tekst
        }

        should("maskere fødselsnummer i en JSON-request") {
            val jsonRequest = """
            {
                "ident": "22345600001",
                "fnr":"12345600001",
                "pid": "01410199999"
            }
        """.trimIndent()
            val expectedRedacted = """
            {
                "ident": "***********",
                "fnr":"***********",
                "pid": "***********"
            }
        """.trimIndent()

            TpUtil.redact(jsonRequest) shouldBe expectedRedacted
        }
    }
})
