package no.nav.pensjon.simulator.core.domain.reglerextend

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.*
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagkildeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import java.time.LocalDate

class ReglerExtensionTest : ShouldSpec({

    should("copy merknad") {
        merknad().let { it.copy() shouldBeEqualToComparingFields it }
    }

    /**
     * NB: Cannot use shouldBeEqualToComparingFields for an entire Pakkseddel object.
     */
    should("copy pakkseddel") {
        val original = pakkseddel()

        with(original.copy()) {
            kontrollTjenesteOk shouldBe true
            annenTjenesteOk shouldBe true
            merknadListe shouldHaveSize 1
            merknadListe[0] shouldBeEqualToComparingFields original.merknadListe[0]
            satstabell shouldBe "s"
        }
    }

    should("copy trygdetidperiode") {
        trygdetidPeriode().let { it.copy() shouldBeEqualToComparingFields it }
    }

    should("copy uføreopptjening") {
        uforeopptjening().let { it.copy() shouldBeEqualToComparingFields it }
    }

    should("copy yrkesskadeopptjening") {
        yrkesskadeopptjening().let { it.copy() shouldBeEqualToComparingFields it }
    }
})

private fun merknad() =
    Merknad().apply {
        kode = "k"
        argumentListe = mutableListOf("a", "b")
    }

private fun pakkseddel() =
    Pakkseddel().apply {
        kontrollTjenesteOk = true
        annenTjenesteOk = true
        merknadListe = listOf(merknad())
        satstabell = "s"
    }

private fun trygdetidPeriode() =
    TTPeriode().also {
        it.fom = LocalDate.of(2021, 1, 1).toNorwegianDateAtNoon()
        it.tom = LocalDate.of(2022, 2, 2).toNorwegianDateAtNoon()
        it.poengIInnAr = true
        it.poengIUtAr = true
        it.landEnum = LandkodeEnum.EST
        it.ikkeProRata = true
        it.bruk = true
        it.grunnlagKildeEnum = GrunnlagkildeEnum.AA
    }

private fun uforeopptjening() =
    Uforeopptjening().apply {
        belop = 1.2
        proRataBeregnetUP = true
        poengtall = 1.3
        ufg = 1
        antattInntekt = 1.4
        antattInntekt_proRata = 1.5
        andel_proRata = 1.6
        poengarTeller_proRata = 2
        poengarNevner_proRata = 3
        antFremtidigeAr_proRata = 4
        yrkesskadeopptjening = yrkesskadeopptjening()
        uforetrygd = true
        konvertertUFT = true
        uforear = true
    }

private fun yrkesskadeopptjening() =
    Yrkesskadeopptjening().apply {
        paa = 1.1
        yug = 1
        antattInntektYrke = 1.2
    }
