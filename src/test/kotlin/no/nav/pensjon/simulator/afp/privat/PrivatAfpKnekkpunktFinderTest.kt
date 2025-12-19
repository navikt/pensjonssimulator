package no.nav.pensjon.simulator.afp.privat

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import java.time.LocalDate
import java.util.*

class PrivatAfpKnekkpunktFinderTest : ShouldSpec({

    should("find 3 knekkpunkter in the 'maximal' case") {
        val expected: SortedSet<LocalDate> = TreeSet()
        expected.add(LocalDate.of(2030, 1, 1)) // første uttaksdato
        expected.add(LocalDate.of(2033, 1, 1)) // 1. januar året bruker blir 63 år
        expected.add(LocalDate.of(2037, 2, 1)) // 1. dag i måneden etter bruker oppnår normert alder

        PrivatAfpKnekkpunktFinder(
            normalderService = Arrange.normalder(foedselsdato = LocalDate.of(1970, 1, 1)),
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

    /**
     * Med fødselsdato 1963-01-15 og normalder 67 år, så oppnås normalder 2030-01-15.
     * 1. dag i måneden etter normalder-oppnåelse blir da 2030-02-01.
     */
    should("finne 1. dag i måneden etter normalder-oppnåelse") {
        val datoer: SortedSet<LocalDate> = PrivatAfpKnekkpunktFinder(
            normalderService = Arrange.normalder(foedselsdato = LocalDate.of(1963, 1, 15)),
            time = mockk<Time>().apply { every { today() } returns LocalDate.of(2025, 1, 1) }
        ).findKnekkpunktDatoer(
            foersteUttakDato = null,
            soekerGrunnlag = Persongrunnlag().apply { fodselsdato = dateAtNoon(1963, Calendar.JANUARY, 15) },
            privatAfpFoersteVirkning = null,
            gjelderOmsorg = false
        )

        datoer.size shouldBe 1
        datoer.first() shouldBe LocalDate.of(2030, 2, 1)
    }
})
