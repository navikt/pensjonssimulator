package no.nav.pensjon.simulator.person

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.person.client.PersonClient

class PersonServiceTest : FunSpec({

    lateinit var client: PersonClient
    lateinit var service: PersonService

    beforeTest {
        client = mockk()
        service = PersonService(client)
    }

    context("person") {

        test("returnerer PenPerson når client finner person") {
            val pid = Pid("12345678901")
            val penPerson = PenPerson(123L)

            every { client.fetchPersonerVedPid(listOf(pid)) } returns mapOf(pid to penPerson)

            val result = service.person(pid)

            result shouldBe penPerson
            result?.penPersonId shouldBe 123L
        }

        test("returnerer null når client ikke finner person") {
            val pid = Pid("12345678901")

            every { client.fetchPersonerVedPid(listOf(pid)) } returns emptyMap()

            val result = service.person(pid)

            result shouldBe null
        }

        test("kaller client med liste som inneholder kun den ene pid") {
            val pid = Pid("98765432109")

            every { client.fetchPersonerVedPid(any()) } returns emptyMap()

            service.person(pid)

            verify { client.fetchPersonerVedPid(listOf(pid)) }
        }

        test("håndterer ulike pid-verdier") {
            val pids = listOf(
                Pid("11111111111"),
                Pid("22222222222"),
                Pid("33333333333")
            )

            pids.forEachIndexed { index, pid ->
                val penPerson = PenPerson((index + 1).toLong())
                every { client.fetchPersonerVedPid(listOf(pid)) } returns mapOf(pid to penPerson)

                val result = service.person(pid)

                result shouldNotBe null
                result?.penPersonId shouldBe (index + 1).toLong()
            }
        }
    }

    context("personListe") {

        test("returnerer tom map når pidListe er tom") {
            every { client.fetchPersonerVedPid(emptyList()) } returns emptyMap()

            val result = service.personListe(emptyList())

            result.size shouldBe 0
        }

        test("returnerer map med én person") {
            val pid = Pid("12345678901")
            val penPerson = PenPerson(100L)

            every { client.fetchPersonerVedPid(listOf(pid)) } returns mapOf(pid to penPerson)

            val result = service.personListe(listOf(pid))

            result.size shouldBe 1
            result[pid] shouldBe penPerson
        }

        test("returnerer map med flere personer") {
            val pid1 = Pid("12345678901")
            val pid2 = Pid("98765432109")
            val pid3 = Pid("11111111111")
            val penPerson1 = PenPerson(1L)
            val penPerson2 = PenPerson(2L)
            val penPerson3 = PenPerson(3L)

            val pidListe = listOf(pid1, pid2, pid3)
            val expectedMap = mapOf(
                pid1 to penPerson1,
                pid2 to penPerson2,
                pid3 to penPerson3
            )

            every { client.fetchPersonerVedPid(pidListe) } returns expectedMap

            val result = service.personListe(pidListe)

            result.size shouldBe 3
            result[pid1] shouldBe penPerson1
            result[pid2] shouldBe penPerson2
            result[pid3] shouldBe penPerson3
        }

        test("delegerer direkte til client") {
            val pidListe = listOf(Pid("12345678901"), Pid("98765432109"))
            val expectedResult = mapOf(
                pidListe[0] to PenPerson(1L),
                pidListe[1] to PenPerson(2L)
            )

            every { client.fetchPersonerVedPid(pidListe) } returns expectedResult

            val result = service.personListe(pidListe)

            result shouldBe expectedResult
            verify(exactly = 1) { client.fetchPersonerVedPid(pidListe) }
        }

        test("returnerer delvis map når ikke alle personer finnes") {
            val pid1 = Pid("12345678901")
            val pid2 = Pid("98765432109")
            val pid3 = Pid("11111111111")
            val penPerson1 = PenPerson(1L)
            val penPerson3 = PenPerson(3L)

            val pidListe = listOf(pid1, pid2, pid3)
            // pid2 finnes ikke
            val partialMap = mapOf(
                pid1 to penPerson1,
                pid3 to penPerson3
            )

            every { client.fetchPersonerVedPid(pidListe) } returns partialMap

            val result = service.personListe(pidListe)

            result.size shouldBe 2
            result[pid1] shouldBe penPerson1
            result[pid2] shouldBe null
            result[pid3] shouldBe penPerson3
        }

        test("returnerer tom map når ingen personer finnes") {
            val pidListe = listOf(Pid("12345678901"), Pid("98765432109"))

            every { client.fetchPersonerVedPid(pidListe) } returns emptyMap()

            val result = service.personListe(pidListe)

            result.size shouldBe 0
        }

        test("håndterer stor liste med pids") {
            val pidListe = (1..100).map { Pid(it.toString().padStart(11, '0')) }
            val expectedMap = pidListe.associateWith { PenPerson(it.value.toLong()) }

            every { client.fetchPersonerVedPid(pidListe) } returns expectedMap

            val result = service.personListe(pidListe)

            result.size shouldBe 100
            verify { client.fetchPersonerVedPid(pidListe) }
        }
    }
})
