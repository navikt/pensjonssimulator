package no.nav.pensjon.simulator.afp.privat

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import java.time.LocalDate
import java.util.*

class PrivatAfpKnekkpunktFinderTest : FunSpec({

    /**
     * Med fødselsdato 1963-01-15 og normalder 67 år, så oppnås normalder 2030-01-15.
     * 1. dag i måneden etter normalder-oppnåelse blir da 2030-02-01.
     */
    test("findKnekkpunktDatoer skal finne 1. dag i måneden etter normalder-oppnåelse") {
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
