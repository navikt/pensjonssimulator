package no.nav.pensjon.simulator.trygdetid

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.util.TreeSet

class TrygdetidUtilTest : FunSpec({

    test("antallAarMedOpptjening gir 0 hvis ingen opptjening") {
        TrygdetidUtil.antallAarMedOpptjening(
            registrerteAarMedOpptjening = TreeSet(), // ingen opptjening
            aarSoekerFikkMinstealderForTrygdetid = 1990,
            dagensDato = LocalDate.of(2025, 1, 1)
        ) shouldBe 0
    }

    test("antallAarMedOpptjening regner ikke med år før minstealder for trygdetid") {
        val opptjeningAarSet = TreeSet<Int>().apply {
            add(1988) // skal ikke medregnes
            add(1989) // skal ikke medregnes
            add(1990)
            add(1991)
            add(1992)
        }

        TrygdetidUtil.antallAarMedOpptjening(
            registrerteAarMedOpptjening = opptjeningAarSet,
            aarSoekerFikkMinstealderForTrygdetid = 1990,
            dagensDato = LocalDate.of(2025, 1, 1)
        ) shouldBe 3
    }

    test("antallAarMedOpptjening regner ikke med år f.o.m. fjoråret") {
        val opptjeningAarSet = TreeSet<Int>().apply {
            add(2022)
            add(2023)
            add(2024) // skal ikke medregnes
            add(2025) // skal ikke medregnes
        }

        TrygdetidUtil.antallAarMedOpptjening(
            registrerteAarMedOpptjening = opptjeningAarSet,
            aarSoekerFikkMinstealderForTrygdetid = 1990,
            dagensDato = LocalDate.of(2025, 12, 15) // fjoråret er 2024
        ) shouldBe 2
    }

    test("antallAarMedOpptjening gir 0 hvis aarSoekerFikkMinstealderForTrygdetid > forrigeAar") {
        val opptjeningAarSet = TreeSet<Int>().apply {
            add(2023)
            add(2024)
            add(2025)
        }

        TrygdetidUtil.antallAarMedOpptjening(
            registrerteAarMedOpptjening = opptjeningAarSet,
            aarSoekerFikkMinstealderForTrygdetid = 2026,
            dagensDato = LocalDate.of(2025, 1, 1) // forrige år er 2024
        ) shouldBe 0
    }
})