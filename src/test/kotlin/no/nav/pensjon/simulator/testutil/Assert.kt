package no.nav.pensjon.simulator.testutil

import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import java.util.Calendar

object Assert {

    fun trygdetidsperiode(periode: TTPeriode, expectedFomAar: Int, expectedTomAar: Int) {
        periode.fom shouldBe dateAtNoon(expectedFomAar, Calendar.JANUARY, 1)
        periode.tom shouldBe dateAtNoon(expectedTomAar, Calendar.DECEMBER, 31)
    }
}