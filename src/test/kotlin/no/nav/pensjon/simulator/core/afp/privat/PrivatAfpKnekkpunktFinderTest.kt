package no.nav.pensjon.simulator.core.afp.privat

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDate
import java.util.*

class PrivatAfpKnekkpunktFinderTest : FunSpec({

    test("findKnekkpunktDatoer should find 3 knekkpunkter in the 'maximal' case") {
        val expected: SortedSet<LocalDate> = TreeSet()
        expected.add(LocalDate.of(2030, 1, 1)) // første uttaksdato
        expected.add(LocalDate.of(2033, 1, 1)) // 1. januar året bruker blir 63 år
        expected.add(LocalDate.of(2037, 2, 1)) // 1. dag i måneden etter bruker oppnår normert alder

        PrivatAfpKnekkpunktFinder(
            normalderService = arrangeNormalder(),
            time = { LocalDate.of(2025, 1, 1) }
        ).findKnekkpunktDatoer(
            foersteUttakDato = LocalDate.of(2030, 1, 1),
            soekerGrunnlag = Persongrunnlag().apply {
                fodselsdato = dateAtNoon(1970, Calendar.JANUARY, 1)
                opptjeningsgrunnlagListe = mutableListOf(
                    Opptjeningsgrunnlag().apply {
                        ar = 2031 // året bruker blir 61 år
                        pi = 123
                    })
            },
            privatAfpFoersteVirkning = LocalDate.of(2030, 1, 1), // = foersteUttakDato
            gjelderOmsorg = false
        ) shouldBe expected
    }
})

private fun arrangeNormalder(): NormertPensjonsalderService =
    mock(NormertPensjonsalderService::class.java).also {
        `when`(it.normalder(foedselsdato = LocalDate.of(1970, 1, 1))).thenReturn(Alder(67, 0))
    }
