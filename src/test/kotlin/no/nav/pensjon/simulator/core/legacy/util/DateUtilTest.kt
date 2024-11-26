package no.nav.pensjon.simulator.core.legacy.util

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class DateUtilTest : FunSpec({

    test("calculateAgeInYears finds number of whole years from last day of 'foedselsdato' month to given date") {
        val foedselsdato = LocalDate.of(1970, 1, 19) // => last day of month = 1970-01-31
        val dato = LocalDate.of(2036, 1, 1) // 65 years 11 months after 1970-01-31
        DateUtil.calculateAgeInYears(foedselsdato, dato) shouldBe 65
    }
})
