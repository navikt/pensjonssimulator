package no.nav.pensjon.simulator.trygdetid

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import java.time.LocalDate

class TrygdetidAdjusterTest : ShouldSpec({

    context("periode-relevans") {
        val idag = LocalDate.of(2025, 6, 1)
        val merEnn2AarSiden = idag.minusYears(2).minusDays(1)

        should("ignorere perioden når den slutter for mer enn 2 år siden") {
            val startdato = LocalDate.of(2023, 1, 1)

            val periodeSomIkkeSkalEndres = TTPeriode().apply {
                fomLd = startdato
                tomLd = merEnn2AarSiden // 2 år og 1 dag siden
                poengIUtAr = true
            }

            TrygdetidAdjuster(time = { idag }).conditionallyAdjustLastTrygdetidPeriode(
                periodeListe = listOf(periodeSomIkkeSkalEndres),
                tom = null // ingen betydning her
            )

            with(periodeSomIkkeSkalEndres) {
                fomLd shouldBe startdato
                tomLd shouldBe merEnn2AarSiden
                poengIUtAr shouldBe true
            }
        }

        should("begrense sluttdato når perioden starter på dagen 2 år siden") {
            val startdato = idag.minusYears(2) // på dagen 2 år siden

            val periodeSomSkalEndres = TTPeriode().apply {
                fomLd = startdato
                tomLd = LocalDate.of(2023, 12, 31)
                poengIUtAr = true
            }

            TrygdetidAdjuster(time = { idag }).conditionallyAdjustLastTrygdetidPeriode(
                periodeListe = listOf(periodeSomSkalEndres),
                tom = null // => gårsdagens dato vil brukes som sluttdato
            )

            with(periodeSomSkalEndres) {
                fomLd shouldBe startdato
                tomLd shouldBe idag.minusDays(1)
                poengIUtAr shouldBe false
            }
        }
    }

    context("begrense sluttdato for trygdetidperioden som har seneste startdato") {
        val idag = LocalDate.of(2025, 6, 15)
        val igaar = idag.minusDays(1)
        val tidligsteStartdato = LocalDate.of(2025, 1, 1)
        val senesteStartdato = LocalDate.of(2025, 2, 1)
        val sluttdatoSomSkalEndres = LocalDate.of(2025, 6, 15)
        val sluttdatoSomIkkeSkalEndres = LocalDate.of(2025, 12, 31)

        val periodeSomSkalEndres = TTPeriode().apply {
            fomLd = senesteStartdato
            tomLd = sluttdatoSomSkalEndres
            poengIUtAr = true
        }

        val periodeSomIkkeSkalEndres = TTPeriode().apply {
            fomLd = tidligsteStartdato
            tomLd = sluttdatoSomIkkeSkalEndres
            poengIUtAr = true
        }

        should("begrense sluttdato til t.o.m.-dato når t.o.m. er i fortid") {
            TrygdetidAdjuster(time = { idag }).conditionallyAdjustLastTrygdetidPeriode(
                periodeListe = listOf(periodeSomIkkeSkalEndres, periodeSomSkalEndres),
                tom = igaar
            )

            with(periodeSomSkalEndres) {
                fomLd shouldBe senesteStartdato
                tomLd shouldBe igaar
                poengIUtAr shouldBe false
            }
            with(periodeSomIkkeSkalEndres) {
                fomLd shouldBe tidligsteStartdato
                tomLd shouldBe sluttdatoSomIkkeSkalEndres
                poengIUtAr shouldBe true
            }
        }

        should("begrense sluttdato til i går når t.o.m. er i framtid") {
            TrygdetidAdjuster(time = { idag }).conditionallyAdjustLastTrygdetidPeriode(
                periodeListe = listOf(periodeSomIkkeSkalEndres, periodeSomSkalEndres),
                tom = idag.plusYears(1) // t.o.m. er i framtid
            )

            with(periodeSomSkalEndres) {
                fomLd shouldBe senesteStartdato
                tomLd shouldBe igaar
                poengIUtAr shouldBe false
            }
            with(periodeSomIkkeSkalEndres) {
                fomLd shouldBe tidligsteStartdato
                tomLd shouldBe sluttdatoSomIkkeSkalEndres
                poengIUtAr shouldBe true
            }
        }
    }
})
