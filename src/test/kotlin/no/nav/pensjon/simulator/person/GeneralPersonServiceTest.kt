package no.nav.pensjon.simulator.person

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.person.client.GeneralPersonClient
import java.time.LocalDate

class GeneralPersonServiceTest : FunSpec({

    lateinit var client: GeneralPersonClient
    lateinit var service: GeneralPersonService

    beforeTest {
        client = mockk()
        service = GeneralPersonService(client)
    }

    context("foedselsdato") {

        test("returnerer fødselsdato når client finner person") {
            val pid = Pid("12345678901")
            val expectedDate = LocalDate.of(1990, 5, 15)

            every { client.fetchFoedselsdato(pid) } returns expectedDate

            val result = service.foedselsdato(pid)

            result shouldBe expectedDate
        }

        test("kaller client med korrekt pid") {
            val pid = Pid("98765432109")
            val date = LocalDate.of(1985, 3, 20)

            every { client.fetchFoedselsdato(pid) } returns date

            service.foedselsdato(pid)

            verify { client.fetchFoedselsdato(pid) }
        }

        test("kaster RuntimeException når client returnerer null") {
            val pid = Pid("12345678901")

            every { client.fetchFoedselsdato(pid) } returns null

            val exception = shouldThrow<RuntimeException> {
                service.foedselsdato(pid)
            }

            exception.message shouldContain "Fødselsdato ikke funnet"
        }

        test("exception inneholder redacted PID") {
            val pid = Pid("12345678901")

            every { client.fetchFoedselsdato(pid) } returns null

            val exception = shouldThrow<RuntimeException> {
                service.foedselsdato(pid)
            }

            // PID should be redacted in the message (123456*****)
            exception.message shouldContain "123456*****"
        }

        test("håndterer ulike fødselsdatoer") {
            val testCases = listOf(
                Pid("11111111111") to LocalDate.of(1950, 1, 1),
                Pid("22222222222") to LocalDate.of(2000, 12, 31),
                Pid("33333333333") to LocalDate.of(1975, 6, 15)
            )

            testCases.forEach { (pid, expectedDate) ->
                every { client.fetchFoedselsdato(pid) } returns expectedDate

                val result = service.foedselsdato(pid)

                result shouldBe expectedDate
            }
        }

        test("håndterer skuddårsdato") {
            val pid = Pid("12345678901")
            val leapYearDate = LocalDate.of(2000, 2, 29)

            every { client.fetchFoedselsdato(pid) } returns leapYearDate

            val result = service.foedselsdato(pid)

            result shouldBe leapYearDate
            result.month.value shouldBe 2
            result.dayOfMonth shouldBe 29
        }

        test("håndterer første dag i året") {
            val pid = Pid("12345678901")
            val firstDayOfYear = LocalDate.of(2024, 1, 1)

            every { client.fetchFoedselsdato(pid) } returns firstDayOfYear

            val result = service.foedselsdato(pid)

            result shouldBe firstDayOfYear
        }

        test("håndterer siste dag i året") {
            val pid = Pid("12345678901")
            val lastDayOfYear = LocalDate.of(2024, 12, 31)

            every { client.fetchFoedselsdato(pid) } returns lastDayOfYear

            val result = service.foedselsdato(pid)

            result shouldBe lastDayOfYear
        }
    }
})
