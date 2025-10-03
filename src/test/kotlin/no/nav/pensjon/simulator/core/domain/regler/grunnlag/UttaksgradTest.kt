package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtMidnight
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import java.time.LocalDate
import java.util.*

class UttaksgradTest : FunSpec({

    val january31 = LocalDate.of(2000, 1, 31)
    val february1 = LocalDate.of(2000, 2, 1)
    val april30 = LocalDate.of(2000, 4, 30)
    val march15 = LocalDate.of(2000, 3, 15)
    val may1 = LocalDate.of(2000, 5, 1)
    val noonFebruary1 = february1.toNorwegianDateAtNoon()
    val noonApril30 = april30.toNorwegianDateAtNoon()

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

    test("tasUt should give true when covering dato") {
        val uttaksgrad = Uttaksgrad().apply {
            fomDato = noonFebruary1
            tomDato = noonApril30
            uttaksgrad = 20
        }

        // På fom-dato:
        uttaksgrad.tasUt(dato = february1) shouldBe true
        // Mellom fom- og tom-dato:
        uttaksgrad.tasUt(dato = march15) shouldBe true
        // På tom-dato:
        uttaksgrad.tasUt(dato = april30) shouldBe true

    }

    test("tasUt should give false when not covering dato") {
        val uttaksgrad = Uttaksgrad().apply {
            fomDato = noonFebruary1
            tomDato = noonApril30
            uttaksgrad = 100
        }

        uttaksgrad.tasUt(dato = january31) shouldBe false
        uttaksgrad.tasUt(dato = may1) shouldBe false
    }

    test("tasUt should give false when grad is zero") {
        val uttaksgrad = Uttaksgrad().apply {
            fomDato = noonFebruary1
            tomDato = noonApril30
            uttaksgrad = 0
        }

        uttaksgrad.tasUt(dato = march15) shouldBe false
    }

    test("tattUtFoer should give true when before dato") {
        val uttaksgrad = Uttaksgrad().apply {
            fomDato = noonFebruary1
            tomDato = noonApril30
            uttaksgrad = 20
        }

        uttaksgrad.tattUtFoer(dato = may1) shouldBe true
    }

    test("tattUtFoer should give false when not before dato") {
        val uttaksgrad = Uttaksgrad().apply {
            fomDato = noonFebruary1
            tomDato = noonApril30
            uttaksgrad = 100
        }

        // På dato:
        uttaksgrad.tattUtFoer(dato = february1) shouldBe false
        // Etter dato:
        uttaksgrad.tattUtFoer(dato = january31) shouldBe false
    }

    test("tattUtFoer should give false when grad is zero") {
        val uttaksgrad = Uttaksgrad().apply {
            fomDato = noonFebruary1
            tomDato = noonApril30
            uttaksgrad = 0
        }

        uttaksgrad.tattUtFoer(dato = may1) shouldBe false
    }
})
