package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtMidnight
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import java.util.*

class UttaksgradTest : FunSpec({

    test("setDatesToNoon should set fom/tom dates to noon") {
        val uttaksgrad = Uttaksgrad().apply {
            fomDato = dateAtMidnight(2021, Calendar.JANUARY, 1)
            tomDato = dateAtMidnight(2021, Calendar.JANUARY, 1)
        }

        uttaksgrad.setDatesToNoon()

        with(uttaksgrad) {
            fomDato shouldBe dateAtNoon(2021, Calendar.JANUARY, 1)
            tomDato shouldBe dateAtNoon(2021, Calendar.JANUARY, 1)
        }
    }
})
