package no.nav.pensjon.simulator.normalder

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.normalder.client.NormertPensjonsalderClient
import no.nav.pensjon.simulator.person.GeneralPersonService
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDate

class NormertPensjonsalderServiceTest : FunSpec({

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
})

private val service =
    NormertPensjonsalderService(
        normalderClient = arrangeClient(),
        personService = mock(GeneralPersonService::class.java)
    )

private fun arrangeClient(): NormertPensjonsalderClient =
    mock(NormertPensjonsalderClient::class.java).also {
        `when`(it.fetchNormalderListe()).thenReturn(
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
        )
    }
