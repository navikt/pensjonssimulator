package no.nav.pensjon.simulator.testutil

import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import java.time.LocalDate

object Assert {

    fun trygdetidsperiode(periode: TTPeriode, expectedFomAar: Int, expectedTomAar: Int) {
        trygdetidsperiode(
            periode,
            expectedFom = LocalDate.of(expectedFomAar, 1, 1),
            expectedTom = LocalDate.of(expectedTomAar, 12, 31)
        )
    }

    fun trygdetidsperiode(periode: TTPeriode, expectedFom: LocalDate, expectedTom: LocalDate) {
        periode.fomLd shouldBe expectedFom
        periode.tomLd shouldBe expectedTom
        periode.landEnum shouldBe LandkodeEnum.NOR
    }
}
