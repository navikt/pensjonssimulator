package no.nav.pensjon.simulator.normalder

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.normalder.client.NormertPensjonsalderClient
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

class NormertPensjonsalderServiceTest : FunSpec({

    context("nedreAlder") {

        test("returnerer nedre aldersgrense for årskull 1964") {
            val result = service.nedreAlder(LocalDate.of(1964, 6, 15))

            result shouldBe Alder(62, 0)
        }

        test("returnerer nedre aldersgrense for årskull 1965") {
            val result = service.nedreAlder(LocalDate.of(1965, 3, 20))

            result shouldBe Alder(62, 1)
        }

        test("returnerer nedre aldersgrense for årskull 1975") {
            val result = service.nedreAlder(LocalDate.of(1975, 12, 1))

            result shouldBe Alder(63, 11)
        }

        test("returnerer nedre aldersgrense uavhengig av måned og dag i fødselsdato") {
            // Same year, different dates should give same result
            val result1 = service.nedreAlder(LocalDate.of(1964, 1, 1))
            val result2 = service.nedreAlder(LocalDate.of(1964, 6, 15))
            val result3 = service.nedreAlder(LocalDate.of(1964, 12, 31))

            result1 shouldBe Alder(62, 0)
            result2 shouldBe Alder(62, 0)
            result3 shouldBe Alder(62, 0)
        }
    }

    context("normalder med fødselsdato") {

        test("returnerer normalder for årskull 1964") {
            val result = service.normalder(LocalDate.of(1964, 6, 15))

            result shouldBe Alder(67, 0)
        }

        test("returnerer normalder for årskull 1965") {
            val result = service.normalder(LocalDate.of(1965, 3, 20))

            result shouldBe Alder(67, 1)
        }

        test("returnerer normalder for årskull 1975") {
            val result = service.normalder(LocalDate.of(1975, 12, 1))

            result shouldBe Alder(68, 11)
        }

        test("returnerer normalder uavhengig av måned og dag i fødselsdato") {
            val result1 = service.normalder(LocalDate.of(1965, 1, 1))
            val result2 = service.normalder(LocalDate.of(1965, 6, 15))
            val result3 = service.normalder(LocalDate.of(1965, 12, 31))

            result1 shouldBe Alder(67, 1)
            result2 shouldBe Alder(67, 1)
            result3 shouldBe Alder(67, 1)
        }
    }

    context("normalder med pid") {

        test("returnerer normalder basert på fødselsdato fra personService") {
            val personService = mockk<GeneralPersonService>()
            val pid = Pid("12345678901")
            every { personService.foedselsdato(pid) } returns LocalDate.of(1964, 6, 15)

            val serviceWithPersonService = NormertPensjonsalderService(
                normalderClient = arrangeClient(),
                personService = personService
            )

            val result = serviceWithPersonService.normalder(pid)

            result shouldBe Alder(67, 0)
            verify { personService.foedselsdato(pid) }
        }

        test("returnerer korrekt normalder for ulike pid-er") {
            val personService = mockk<GeneralPersonService>()
            val pid1 = Pid("11111111111")
            val pid2 = Pid("22222222222")
            every { personService.foedselsdato(pid1) } returns LocalDate.of(1965, 3, 20)
            every { personService.foedselsdato(pid2) } returns LocalDate.of(1975, 8, 10)

            val serviceWithPersonService = NormertPensjonsalderService(
                normalderClient = arrangeClient(),
                personService = personService
            )

            serviceWithPersonService.normalder(pid1) shouldBe Alder(67, 1)
            serviceWithPersonService.normalder(pid2) shouldBe Alder(68, 11)
        }
    }

    context("normalderDato") {

        test("returnerer uttaksdato ved normalder for årskull 1964") {
            // 1964-06-15 + 67 år 0 måneder = 2031-06-15 => uttaksdato er 1. i neste måned = 2031-07-01
            val result = service.normalderDato(LocalDate.of(1964, 6, 15))

            result shouldBe LocalDate.of(2031, 7, 1)
        }

        test("returnerer uttaksdato ved normalder for årskull 1965") {
            // 1965-03-20 + 67 år 1 måned = 2032-04-20 => uttaksdato er 1. i neste måned = 2032-05-01
            val result = service.normalderDato(LocalDate.of(1965, 3, 20))

            result shouldBe LocalDate.of(2032, 5, 1)
        }

        test("returnerer uttaksdato ved normalder for årskull 1975") {
            // 1975-12-01 + 68 år 11 måneder = 2044-11-01 => uttaksdato er 1. i neste måned = 2044-12-01
            val result = service.normalderDato(LocalDate.of(1975, 12, 1))

            result shouldBe LocalDate.of(2044, 12, 1)
        }

        test("returnerer uttaksdato som første dag i måneden etter normalder oppnås") {
            // 1964-01-15 + 67 år 0 måneder = 2031-01-15 => uttaksdato = 2031-02-01
            val result = service.normalderDato(LocalDate.of(1964, 1, 15))

            result shouldBe LocalDate.of(2031, 2, 1)
        }

        test("håndterer fødselsdag på siste dag i måneden") {
            // 1964-01-31 + 67 år 0 måneder = 2031-01-31 => uttaksdato = 2031-02-01
            val result = service.normalderDato(LocalDate.of(1964, 1, 31))

            result shouldBe LocalDate.of(2031, 2, 1)
        }

        test("håndterer årskifte ved normalder") {
            // 1964-12-15 + 67 år 0 måneder = 2031-12-15 => uttaksdato = 2032-01-01
            val result = service.normalderDato(LocalDate.of(1964, 12, 15))

            result shouldBe LocalDate.of(2032, 1, 1)
        }

        test("håndterer normalder med måneder som fører til årskifte") {
            // 1965-11-15 + 67 år 1 måned = 2032-12-15 => uttaksdato = 2033-01-01
            val result = service.normalderDato(LocalDate.of(1965, 11, 15))

            result shouldBe LocalDate.of(2033, 1, 1)
        }
    }

    test("opptjeningMaxAlderAar med fødselsdag i desember") {
        service.opptjeningMaxAlderAar(foedselsdato = LocalDate.of(1964, 12, 31)) shouldBe 75
        service.opptjeningMaxAlderAar(foedselsdato = LocalDate.of(1975, 12, 30)) shouldBe 76
    }

    test("opptjeningMaxAlderAar med fødselsdag i januar") {
        service.opptjeningMaxAlderAar(foedselsdato = LocalDate.of(1965, 1, 2)) shouldBe 75
        service.opptjeningMaxAlderAar(foedselsdato = LocalDate.of(1975, 1, 1)) shouldBe 76
    }

    test("normertPensjoneringsdato med fødselsdag i desember") {
        // 1964-12-31 + 67:0 = 2031-12-31 => pensjonering starter 1. neste måned = 2032-01-01
        service.normertPensjoneringsdato(foedselsdato = LocalDate.of(1964, 12, 31)) shouldBe LocalDate.of(2032, 1, 1)

        // 1965-12-01 + 67:1 = 2033-01-01 => pensjonering starter 1. neste måned = 2033-02-01
        service.normertPensjoneringsdato(foedselsdato = LocalDate.of(1965, 12, 1)) shouldBe LocalDate.of(2033, 2, 1)

        // 1975-12-15 + 68:11 = 2044-11-15 => pensjonering starter 1. neste måned = 2044-12-01
        service.normertPensjoneringsdato(foedselsdato = LocalDate.of(1975, 12, 15)) shouldBe LocalDate.of(2044, 12, 1)
    }

    test("normertPensjoneringsdato med fødselsdag i januar") {
        service.normertPensjoneringsdato(foedselsdato = LocalDate.of(1964, 1, 31)) shouldBe LocalDate.of(2031, 2, 1)
        service.normertPensjoneringsdato(foedselsdato = LocalDate.of(1965, 1, 1)) shouldBe LocalDate.of(2032, 3, 1)
        service.normertPensjoneringsdato(foedselsdato = LocalDate.of(1975, 1, 15)) shouldBe LocalDate.of(2044, 1, 1)
    }

    test("normalderOppnaasDato med fødselsdag i desember") {
        service.normalderOppnaasDato(foedselsdato = LocalDate.of(1964, 12, 31)) shouldBe LocalDate.of(2031, 12, 31)
        service.normalderOppnaasDato(foedselsdato = LocalDate.of(1965, 12, 30)) shouldBe LocalDate.of(2033, 1, 30)
        service.normalderOppnaasDato(foedselsdato = LocalDate.of(1975, 12, 1)) shouldBe LocalDate.of(2044, 11, 1)
    }

    test("normalderOppnaasDato med fødselsdag i januar") {
        service.normalderOppnaasDato(foedselsdato = LocalDate.of(1964, 1, 15)) shouldBe LocalDate.of(2031, 1, 15)
        service.normalderOppnaasDato(foedselsdato = LocalDate.of(1965, 1, 1)) shouldBe LocalDate.of(2032, 2, 1)
        service.normalderOppnaasDato(foedselsdato = LocalDate.of(1975, 1, 31)) shouldBe LocalDate.of(2043, 12, 31)
    }

    test("oevreAlderOppnaasDato med fødselsdag i desember") {
        service.oevreAlderOppnaasDato(foedselsdato = LocalDate.of(1964, 12, 31)) shouldBe LocalDate.of(2039, 12, 31)
        service.oevreAlderOppnaasDato(foedselsdato = LocalDate.of(1965, 12, 30)) shouldBe LocalDate.of(2041, 1, 30)
        service.oevreAlderOppnaasDato(foedselsdato = LocalDate.of(1975, 12, 1)) shouldBe LocalDate.of(2052, 11, 1)
    }

    test("oevreAlderOppnaasDato med fødselsdag i januar") {
        service.oevreAlderOppnaasDato(foedselsdato = LocalDate.of(1964, 1, 15)) shouldBe LocalDate.of(2039, 1, 15)
        service.oevreAlderOppnaasDato(foedselsdato = LocalDate.of(1965, 1, 1)) shouldBe LocalDate.of(2040, 2, 1)
        service.oevreAlderOppnaasDato(foedselsdato = LocalDate.of(1975, 1, 31)) shouldBe LocalDate.of(2051, 12, 31)
    }
})

private val service =
    NormertPensjonsalderService(
        normalderClient = arrangeClient(),
        personService = mockk()
    )

private fun arrangeClient(): NormertPensjonsalderClient =
    mockk<NormertPensjonsalderClient>().apply {
        every { fetchNormalderListe() } returns
                listOf(
                    Aldersgrenser(
                        aarskull = 1964,
                        normalder = Alder(67, 0),
                        nedreAlder = Alder(62, 0),
                        oevreAlder = Alder(75, 0),
                        verdiStatus = VerdiStatus.PROGNOSE
                    ),
                    Aldersgrenser(
                        aarskull = 1965,
                        normalder = Alder(67, 1),
                        nedreAlder = Alder(62, 1),
                        oevreAlder = Alder(75, 1),
                        verdiStatus = VerdiStatus.PROGNOSE
                    ),
                    Aldersgrenser(
                        aarskull = 1975,
                        normalder = Alder(68, 11),
                        nedreAlder = Alder(63, 11),
                        oevreAlder = Alder(76, 11),
                        verdiStatus = VerdiStatus.PROGNOSE
                    )
                )
    }
