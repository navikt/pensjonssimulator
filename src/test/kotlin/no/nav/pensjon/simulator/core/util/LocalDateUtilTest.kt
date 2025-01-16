package no.nav.pensjon.simulator.core.util

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.util.*

class LocalDateUtilTest : FunSpec({
    val defaultTimeZone = TimeZone.getDefault()

    afterSpec {
        TimeZone.setDefault(defaultTimeZone)
    }

    test("norwegianDate should have 1 as hour-of-day in Helsinki") {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Helsinki"))
        val date = LocalDateUtil.norwegianDate(LocalDate.of(2024, 5, 6))
        Calendar.getInstance().apply { time = date }.get(Calendar.HOUR_OF_DAY) shouldBe 1
    }

    test("norwegianDateAtNoon should have 11 as hour-of-day in London") {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"))
        val date = LocalDateUtil.norwegianDateAtNoon(LocalDate.of(2024, 5, 6))
        Calendar.getInstance().apply { time = date }.get(Calendar.HOUR_OF_DAY) shouldBe 11
    }
})
