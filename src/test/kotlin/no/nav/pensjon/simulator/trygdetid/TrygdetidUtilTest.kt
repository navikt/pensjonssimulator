package no.nav.pensjon.simulator.trygdetid

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import java.time.LocalDate
import java.util.TreeSet

class TrygdetidUtilTest : FunSpec({

    context("antallAarMedOpptjening") {

        test("gir 0 hvis ingen opptjening") {
            TrygdetidUtil.antallAarMedOpptjening(
                registrerteAarMedOpptjening = TreeSet(), // ingen opptjening
                aarSoekerFikkMinstealderForTrygdetid = 1990,
                dagensDato = LocalDate.of(2025, 1, 1)
            ) shouldBe 0
        }

        test("regner ikke med år før minstealder for trygdetid") {
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

        test("regner ikke med år f.o.m. fjoråret") {
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

        test("gir 0 hvis aarSoekerFikkMinstealderForTrygdetid > forrigeAar") {
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
    }

    context("trygdetidSpec") {

        test("setter alle felt korrekt når soekerFoersteVirkningFom er oppgitt") {
            val persongrunnlag = Persongrunnlag()
            val uttaksgrad = Uttaksgrad()
            val kravhode = Kravhode().apply {
                regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_N_OPPTJ
                uttaksgradListe = mutableListOf(uttaksgrad)
            }
            val knekkpunktDato = LocalDate.of(2030, 7, 1)
            val soekerFoersteVirkningFom = LocalDate.of(2028, 1, 1)

            val result = TrygdetidUtil.trygdetidSpec(
                kravhode = kravhode,
                persongrunnlag = persongrunnlag,
                knekkpunktDato = knekkpunktDato,
                soekerFoersteVirkningFom = soekerFoersteVirkningFom,
                ytelseType = KravlinjeTypeEnum.AP,
                boddEllerArbeidetUtenlands = true
            )

            result.virkFom shouldBe knekkpunktDato.toNorwegianDateAtNoon()
            result.brukerForsteVirk shouldBe soekerFoersteVirkningFom.toNorwegianDateAtNoon()
            result.hovedKravlinjeType shouldBe KravlinjeTypeEnum.AP
            result.persongrunnlag shouldBe persongrunnlag
            result.boddEllerArbeidetIUtlandet shouldBe true
            result.regelverkTypeEnum shouldBe RegelverkTypeEnum.N_REG_G_N_OPPTJ
            result.uttaksgradListe shouldBe kravhode.uttaksgradListe
        }

        test("setter brukerForsteVirk til null når soekerFoersteVirkningFom er null (f.eks. for avdød)") {
            val persongrunnlag = Persongrunnlag()
            val kravhode = Kravhode().apply {
                regelverkTypeEnum = RegelverkTypeEnum.N_REG_N_OPPTJ
            }
            val knekkpunktDato = LocalDate.of(2030, 7, 1)

            val result = TrygdetidUtil.trygdetidSpec(
                kravhode = kravhode,
                persongrunnlag = persongrunnlag,
                knekkpunktDato = knekkpunktDato,
                soekerFoersteVirkningFom = null, // null for avdød
                ytelseType = KravlinjeTypeEnum.GJP,
                boddEllerArbeidetUtenlands = false
            )

            result.virkFom shouldBe knekkpunktDato.toNorwegianDateAtNoon()
            result.brukerForsteVirk shouldBe null
            result.hovedKravlinjeType shouldBe KravlinjeTypeEnum.GJP
            result.persongrunnlag shouldBe persongrunnlag
            result.boddEllerArbeidetIUtlandet shouldBe false
            result.regelverkTypeEnum shouldBe RegelverkTypeEnum.N_REG_N_OPPTJ
        }

        test("kopierer uttaksgradListe fra kravhode") {
            val uttaksgrad1 = Uttaksgrad().apply { uttaksgrad = 50 }
            val uttaksgrad2 = Uttaksgrad().apply { uttaksgrad = 100 }
            val persongrunnlag = Persongrunnlag()
            val kravhode = Kravhode().apply {
                regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_N_OPPTJ
                uttaksgradListe = mutableListOf(uttaksgrad1, uttaksgrad2)
            }

            val result = TrygdetidUtil.trygdetidSpec(
                kravhode = kravhode,
                persongrunnlag = persongrunnlag,
                knekkpunktDato = LocalDate.of(2030, 1, 1),
                soekerFoersteVirkningFom = LocalDate.of(2028, 1, 1),
                ytelseType = KravlinjeTypeEnum.AP,
                boddEllerArbeidetUtenlands = true
            )

            result.uttaksgradListe.size shouldBe 2
            result.uttaksgradListe[0].uttaksgrad shouldBe 50
            result.uttaksgradListe[1].uttaksgrad shouldBe 100
        }

        test("håndterer tom uttaksgradListe") {
            val persongrunnlag = Persongrunnlag()
            val kravhode = Kravhode().apply {
                regelverkTypeEnum = RegelverkTypeEnum.N_REG_N_OPPTJ
                uttaksgradListe = mutableListOf() // tom liste
            }

            val result = TrygdetidUtil.trygdetidSpec(
                kravhode = kravhode,
                persongrunnlag = persongrunnlag,
                knekkpunktDato = LocalDate.of(2030, 1, 1),
                soekerFoersteVirkningFom = LocalDate.of(2028, 1, 1),
                ytelseType = KravlinjeTypeEnum.AP,
                boddEllerArbeidetUtenlands = false
            )

            result.uttaksgradListe.size shouldBe 0
        }
    }
})