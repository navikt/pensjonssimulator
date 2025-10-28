package no.nav.pensjon.simulator.trygdetid

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import java.time.LocalDate

class TrygdetidAdjusterTest : ShouldSpec({

    context("periode-relevans") {
        val idag = LocalDate.of(2025, 6, 1)
        val merEnn2AarSiden = idag.minusYears(2).minusDays(1).toNorwegianDateAtNoon()

        should("ignorere perioden når den slutter for mer enn 2 år siden") {
            val startdato = LocalDate.of(2023, 1, 1).toNorwegianDateAtNoon()

            val periodeSomIkkeSkalEndres = TTPeriode().apply {
                fom = startdato
                tom = merEnn2AarSiden // 2 år og 1 dag siden
                poengIUtAr = true
            }

            TrygdetidAdjuster(time = { idag }).conditionallyAdjustLastTrygdetidPeriode(
                periodeListe = listOf(periodeSomIkkeSkalEndres),
                tom = null // ingen betydning her
            )

            with(periodeSomIkkeSkalEndres) {
                fom shouldBe startdato
                tom shouldBe merEnn2AarSiden
                poengIUtAr shouldBe true
            }
        }

        should("begrense sluttdato når perioden starter på dagen 2 år siden") {
            val startdato = idag.minusYears(2).toNorwegianDateAtNoon() // på dagen 2 år siden

            val periodeSomSkalEndres = TTPeriode().apply {
                fom = startdato
                tom = LocalDate.of(2023, 12, 31).toNorwegianDateAtNoon()
                poengIUtAr = true
            }

            TrygdetidAdjuster(time = { idag }).conditionallyAdjustLastTrygdetidPeriode(
                periodeListe = listOf(periodeSomSkalEndres),
                tom = null // => gårsdagens dato vil brukes som sluttdato
            )

            with(periodeSomSkalEndres) {
                fom shouldBe startdato
                tom shouldBe idag.minusDays(1).toNorwegianDateAtNoon()
                poengIUtAr shouldBe false
            }
        }
    }

    context("begrense sluttdato for trygdetidperioden som har seneste startdato") {
        val idag = LocalDate.of(2025, 6, 15)
        val igaar = idag.minusDays(1)
        val tidligsteStartdato = LocalDate.of(2025, 1, 1).toNorwegianDateAtNoon()
        val senesteStartdato = LocalDate.of(2025, 2, 1).toNorwegianDateAtNoon()
        val sluttdatoSomSkalEndres = LocalDate.of(2025, 6, 15).toNorwegianDateAtNoon()
        val sluttdatoSomIkkeSkalEndres = LocalDate.of(2025, 12, 31).toNorwegianDateAtNoon()

        val periodeSomSkalEndres = TTPeriode().apply {
            fom = senesteStartdato
            tom = sluttdatoSomSkalEndres
            poengIUtAr = true
        }

        val periodeSomIkkeSkalEndres = TTPeriode().apply {
            fom = tidligsteStartdato
            tom = sluttdatoSomIkkeSkalEndres
            poengIUtAr = true
        }

        should("begrense sluttdato til t.o.m.-dato når t.o.m. er i fortid") {
            TrygdetidAdjuster(time = { idag }).conditionallyAdjustLastTrygdetidPeriode(
                periodeListe = listOf(periodeSomIkkeSkalEndres, periodeSomSkalEndres),
                tom = igaar
            )

            with(periodeSomSkalEndres) {
                fom shouldBe senesteStartdato
                tom shouldBe igaar.toNorwegianDateAtNoon()
                poengIUtAr shouldBe false
            }
            with(periodeSomIkkeSkalEndres) {
                fom shouldBe tidligsteStartdato
                tom shouldBe sluttdatoSomIkkeSkalEndres
                poengIUtAr shouldBe true
            }
        }

        should("begrense sluttdato til i går når t.o.m. er i framtid") {
            TrygdetidAdjuster(time = { idag }).conditionallyAdjustLastTrygdetidPeriode(
                periodeListe = listOf(periodeSomIkkeSkalEndres, periodeSomSkalEndres),
                tom = idag.plusYears(1) // t.o.m. er i framtid
            )

            with(periodeSomSkalEndres) {
                fom shouldBe senesteStartdato
                tom shouldBe igaar.toNorwegianDateAtNoon()
                poengIUtAr shouldBe false
            }
            with(periodeSomIkkeSkalEndres) {
                fom shouldBe tidligsteStartdato
                tom shouldBe sluttdatoSomIkkeSkalEndres
                poengIUtAr shouldBe true
            }
        }
    }
})
