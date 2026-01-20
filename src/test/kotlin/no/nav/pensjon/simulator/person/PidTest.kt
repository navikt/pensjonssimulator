package no.nav.pensjon.simulator.person

import com.fasterxml.jackson.annotation.JsonIgnore
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import tools.jackson.databind.json.JsonMapper

/**
 * Alle fødselsnumre og D-numre brukt her er syntetiske/fiktive.
 */
class PidTest : ShouldSpec({

    context("value") {
        should("return 'invalid' for invalid value") {
            Pid("bad value").value shouldBe "invalid"
        }

        should("return PID value for valid value") {
            Pid("04925398980").value shouldBe "04925398980"
        }
    }

    context("displayValue") {
        should("return 'invalid' for invalid value") {
            Pid("bad value").displayValue shouldBe "?(7)?"
        }

        should("return redacted value for valid value") {
            Pid("04925398980").displayValue shouldBe "049253*****"
        }
    }

    context("toString") {
        should("return 'invalid' for invalid value") {
            Pid("bad value").toString() shouldBe "?(7)?"
        }

        should("return redacted value for valid value") {
            Pid("04925398980").toString() shouldBe "049253*****"
        }
    }

    context("equals") {
        should("be true when string values are equal") {
            (Pid("04925398980") == Pid("04925398980")) shouldBe true
        }

        should("be false when string values are not equal") {
            (Pid("04925398980") == Pid("12906498357")) shouldBe false
        }

        should("be false when values are not both PID") {
            (Pid("04925398980").equals("04925398980")) shouldBe false
        }

        should("be false when value is null") {
            Pid("04925398980").equals(null) shouldBe false
        }
    }

    /**
     * As of 2026-01-19, this test will fail if Pid is made an inline value class.
     */
    context("JSON serialisation") {
        should("be ignored when serialising value marked with @JsonIgnore") {
            val wrapper = PidWrapper(pid = Pid("04925398980"))
            wrapper.pid.value shouldBe "04925398980"
            jsonMapper.writeValueAsString(wrapper) shouldBe "{}" // i.e., ignored
        }
    }
})

private class PidWrapper(@JsonIgnore val pid: Pid)

private val jsonMapper = JsonMapper()
