package no.nav.pensjon.simulator.validity

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import tools.jackson.databind.exc.MismatchedInputException
import tools.jackson.databind.json.JsonMapper
import java.time.format.DateTimeParseException

class IngressErrorHandlerTest : ShouldSpec({

    context("extractExceptionNames") {
        should("ikke returnere mulig sensitiv exception message") {
            IngressErrorHandler.extractExceptionNames(e = RuntimeException("sensitiv info")) shouldBe "RuntimeException"
        }

        should("ikke inkludere mulig sensitiv årsak") {
            val cause = IllegalArgumentException("sensitiv rotårsak")

            IngressErrorHandler.extractExceptionNames(
                e = RuntimeException("Outer error", cause)
            ) shouldBe "RuntimeException | Cause: IllegalArgumentException"
        }

        should("håndtere flere nivåer av årsaker") {
            val rootCause = IllegalStateException("Root")
            val middleCause = IllegalArgumentException("Middle", rootCause)
            val exception = RuntimeException("Top", middleCause)

            IngressErrorHandler.extractExceptionNames(exception) shouldBe
                    "RuntimeException | Cause: IllegalArgumentException | Cause: IllegalStateException"
        }

        should("bruke klassenavn når message er udefinert") {
            IngressErrorHandler.extractExceptionNames(e = RuntimeException()) shouldBe "RuntimeException"
        }
    }

    context("extractUnsafeMessages") {
        should("håndtere flere nivåer av årsaker") {
            val rootCause = IllegalStateException("Root")
            val middleCause = IllegalArgumentException("Middle", rootCause)
            val exception = RuntimeException("Top", middleCause)

            IngressErrorHandler.extractUnsafeMessages(exception) shouldBe "Top | Cause: Middle | Cause: Root"
        }

        should("bruke klassenavn når message er udefinert") {
            val rootCause = NumberFormatException()
            val exception = IllegalArgumentException(null, rootCause)

            IngressErrorHandler.extractUnsafeMessages(exception) shouldBe "IllegalArgumentException | Cause: NumberFormatException"
        }
    }

    context("extractSafeMessage") {
        should("betrakte DateTimeParseException som sikker") {
            val exception = DateTimeParseException("Oops", "2023.01.01", 4)
            IngressErrorHandler.extractSafeMessage(exception) shouldBe "Oops"
        }

        should("betrakte NullPointerException som sikker, men fjerne intern info") {
            val exception =
                NullPointerException("Parameter specified as non-null is null: method internal.info.MyClass.<init>, parameter personId")
            IngressErrorHandler.extractSafeMessage(exception) shouldBe "Missing required parameter: personId"
        }

        should("betrakte MismatchedInputException som sikker og håndtere flere nivåer av årsaker") {
            try {
                JsonMapper().readerFor(TestClass::class.java).readValue<TestClass>("{}")
            } catch (rootCause: MismatchedInputException) {
                val middleCause = IllegalArgumentException("Middle", rootCause)
                val exception = RuntimeException("Oops", middleCause)
                IngressErrorHandler.extractSafeMessage(exception) shouldBe rootCause.message
            }
        }
    }
})

private data class TestClass(
    val number: Int
)