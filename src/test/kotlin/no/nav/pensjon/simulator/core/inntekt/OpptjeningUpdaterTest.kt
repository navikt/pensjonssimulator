package no.nav.pensjon.simulator.core.inntekt

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagkildeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.OpptjeningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.krav.Inntekt
import java.time.LocalDate

class OpptjeningUpdaterTest : FunSpec({

    test("oppdaterOpptjeningsgrunnlagFraInntekter returnerer original grunnlag liste når inntekt liste er tom") {
        val context = mockk<SimulatorContext>()
        every { context.beregnPoengtallBatch(any(), any()) } returns mutableListOf()

        val updater = OpptjeningUpdater(context)
        val originalGrunnlag = listOf(
            Opptjeningsgrunnlag().apply {
                ar = 2020
                pi = 500000
                opptjeningTypeEnum = OpptjeningtypeEnum.PPI
            }
        )

        val result = updater.oppdaterOpptjeningsgrunnlagFraInntekter(
            originalGrunnlagListe = originalGrunnlag,
            inntektListe = emptyList(),
            foedselsdato = LocalDate.of(1963, 1, 1)
        )

        result shouldHaveSize 1
        result[0].ar shouldBe 2020
        result[0].pi shouldBe 500000
    }

    test("oppdaterOpptjeningsgrunnlagFraInntekter filtrerer ut inntekter med beloep = 0") {
        val context = mockk<SimulatorContext>()
        val grunnlagSlot = slot<MutableList<Opptjeningsgrunnlag>>()
        every { context.beregnPoengtallBatch(capture(grunnlagSlot), any()) } answers { grunnlagSlot.captured }

        val updater = OpptjeningUpdater(context)
        val inntekter = listOf(
            Inntekt(inntektAar = 2020, beloep = 0L),
            Inntekt(inntektAar = 2021, beloep = 500000L)
        )

        val result = updater.oppdaterOpptjeningsgrunnlagFraInntekter(
            originalGrunnlagListe = emptyList(),
            inntektListe = inntekter,
            foedselsdato = LocalDate.of(1963, 1, 1)
        )

        result shouldHaveSize 1
        result[0].ar shouldBe 2021
        result[0].pi shouldBe 500000
    }

    test("oppdaterOpptjeningsgrunnlagFraInntekter filtrerer ut inntekter med negativ beloep") {
        val context = mockk<SimulatorContext>()
        val grunnlagSlot = slot<MutableList<Opptjeningsgrunnlag>>()
        every { context.beregnPoengtallBatch(capture(grunnlagSlot), any()) } answers { grunnlagSlot.captured }

        val updater = OpptjeningUpdater(context)
        val inntekter = listOf(
            Inntekt(inntektAar = 2020, beloep = -100L),
            Inntekt(inntektAar = 2021, beloep = 500000L)
        )

        val result = updater.oppdaterOpptjeningsgrunnlagFraInntekter(
            originalGrunnlagListe = emptyList(),
            inntektListe = inntekter,
            foedselsdato = LocalDate.of(1963, 1, 1)
        )

        result shouldHaveSize 1
        result[0].ar shouldBe 2021
    }

    test("oppdaterOpptjeningsgrunnlagFraInntekter konverterer inntekt til Opptjeningsgrunnlag med korrekte verdier") {
        val context = mockk<SimulatorContext>()
        val grunnlagSlot = slot<MutableList<Opptjeningsgrunnlag>>()
        every { context.beregnPoengtallBatch(capture(grunnlagSlot), any()) } answers { grunnlagSlot.captured }

        val updater = OpptjeningUpdater(context)
        val inntekter = listOf(
            Inntekt(inntektAar = 2022, beloep = 650000L)
        )

        updater.oppdaterOpptjeningsgrunnlagFraInntekter(
            originalGrunnlagListe = emptyList(),
            inntektListe = inntekter,
            foedselsdato = LocalDate.of(1963, 1, 1)
        )

        val capturedGrunnlag = grunnlagSlot.captured
        capturedGrunnlag shouldHaveSize 1
        capturedGrunnlag[0].ar shouldBe 2022
        capturedGrunnlag[0].pi shouldBe 650000
        capturedGrunnlag[0].opptjeningTypeEnum shouldBe OpptjeningtypeEnum.PPI
        capturedGrunnlag[0].bruk shouldBe true
    }

    test("oppdaterOpptjeningsgrunnlagFraInntekter kaller beregnPoengtallBatch med foedselsdato") {
        val context = mockk<SimulatorContext>()
        val grunnlagSlot = slot<MutableList<Opptjeningsgrunnlag>>()
        every { context.beregnPoengtallBatch(capture(grunnlagSlot), any()) } returns mutableListOf()

        val updater = OpptjeningUpdater(context)
        val foedselsdato = LocalDate.of(1965, 5, 15)

        updater.oppdaterOpptjeningsgrunnlagFraInntekter(
            originalGrunnlagListe = emptyList(),
            inntektListe = listOf(Inntekt(inntektAar = 2022, beloep = 500000L)),
            foedselsdato = foedselsdato
        )

        verify { context.beregnPoengtallBatch(any(), foedselsdato) }
    }

    test("oppdaterOpptjeningsgrunnlagFraInntekter setter bruk=true og grunnlagKildeEnum=BRUKER på resultatet") {
        val context = mockk<SimulatorContext>()
        every { context.beregnPoengtallBatch(any(), any()) } answers {
            val grunnlag = firstArg<MutableList<Opptjeningsgrunnlag>>()
            grunnlag.forEach { it.bruk = false; it.grunnlagKildeEnum = null }
            grunnlag
        }

        val updater = OpptjeningUpdater(context)
        val inntekter = listOf(
            Inntekt(inntektAar = 2022, beloep = 500000L)
        )

        val result = updater.oppdaterOpptjeningsgrunnlagFraInntekter(
            originalGrunnlagListe = emptyList(),
            inntektListe = inntekter,
            foedselsdato = LocalDate.of(1963, 1, 1)
        )

        result shouldHaveSize 1
        result[0].bruk shouldBe true
        result[0].grunnlagKildeEnum shouldBe GrunnlagkildeEnum.BRUKER
    }

    test("oppdaterOpptjeningsgrunnlagFraInntekter kombinerer original grunnlag med nye inntektsbaserte grunnlag") {
        val context = mockk<SimulatorContext>()
        val grunnlagSlot = slot<MutableList<Opptjeningsgrunnlag>>()
        every { context.beregnPoengtallBatch(capture(grunnlagSlot), any()) } answers { grunnlagSlot.captured }

        val updater = OpptjeningUpdater(context)
        val originalGrunnlag = listOf(
            Opptjeningsgrunnlag().apply {
                ar = 2019
                pi = 400000
                opptjeningTypeEnum = OpptjeningtypeEnum.OBU7
                grunnlagKildeEnum = GrunnlagkildeEnum.POPP
            }
        )
        val inntekter = listOf(
            Inntekt(inntektAar = 2022, beloep = 600000L)
        )

        val result = updater.oppdaterOpptjeningsgrunnlagFraInntekter(
            originalGrunnlagListe = originalGrunnlag,
            inntektListe = inntekter,
            foedselsdato = LocalDate.of(1963, 1, 1)
        )

        result shouldHaveSize 2
        result[0].ar shouldBe 2019
        result[0].pi shouldBe 400000
        result[0].opptjeningTypeEnum shouldBe OpptjeningtypeEnum.OBU7
        result[0].grunnlagKildeEnum shouldBe GrunnlagkildeEnum.POPP
        result[1].ar shouldBe 2022
        result[1].pi shouldBe 600000
        result[1].grunnlagKildeEnum shouldBe GrunnlagkildeEnum.BRUKER
    }

    test("oppdaterOpptjeningsgrunnlagFraInntekter håndterer flere inntekter") {
        val context = mockk<SimulatorContext>()
        val grunnlagSlot = slot<MutableList<Opptjeningsgrunnlag>>()
        every { context.beregnPoengtallBatch(capture(grunnlagSlot), any()) } answers { grunnlagSlot.captured }

        val updater = OpptjeningUpdater(context)
        val inntekter = listOf(
            Inntekt(inntektAar = 2020, beloep = 400000L),
            Inntekt(inntektAar = 2021, beloep = 500000L),
            Inntekt(inntektAar = 2022, beloep = 600000L)
        )

        val result = updater.oppdaterOpptjeningsgrunnlagFraInntekter(
            originalGrunnlagListe = emptyList(),
            inntektListe = inntekter,
            foedselsdato = LocalDate.of(1963, 1, 1)
        )

        result shouldHaveSize 3
        result[0].ar shouldBe 2020
        result[0].pi shouldBe 400000
        result[1].ar shouldBe 2021
        result[1].pi shouldBe 500000
        result[2].ar shouldBe 2022
        result[2].pi shouldBe 600000
    }

    test("oppdaterOpptjeningsgrunnlagFraInntekter returnerer tom liste når original er tom og alle inntekter filtreres ut") {
        val context = mockk<SimulatorContext>()
        every { context.beregnPoengtallBatch(any(), any()) } returns mutableListOf()

        val updater = OpptjeningUpdater(context)
        val inntekter = listOf(
            Inntekt(inntektAar = 2020, beloep = 0L),
            Inntekt(inntektAar = 2021, beloep = 0L)
        )

        val result = updater.oppdaterOpptjeningsgrunnlagFraInntekter(
            originalGrunnlagListe = emptyList(),
            inntektListe = inntekter,
            foedselsdato = LocalDate.of(1963, 1, 1)
        )

        result.shouldBeEmpty()
    }

    test("oppdaterOpptjeningsgrunnlagFraInntekter håndterer null foedselsdato") {
        val context = mockk<SimulatorContext>()
        val grunnlagSlot = slot<MutableList<Opptjeningsgrunnlag>>()
        every { context.beregnPoengtallBatch(capture(grunnlagSlot), any()) } answers { grunnlagSlot.captured }

        val updater = OpptjeningUpdater(context)
        val inntekter = listOf(
            Inntekt(inntektAar = 2022, beloep = 500000L)
        )

        val result = updater.oppdaterOpptjeningsgrunnlagFraInntekter(
            originalGrunnlagListe = emptyList(),
            inntektListe = inntekter,
            foedselsdato = null
        )

        result shouldHaveSize 1
        verify { context.beregnPoengtallBatch(any(), null) }
    }

    test("oppdaterOpptjeningsgrunnlagFraInntekter bevarer beregnede verdier fra beregnPoengtallBatch") {
        val context = mockk<SimulatorContext>()
        every { context.beregnPoengtallBatch(any(), any()) } answers {
            val grunnlag = firstArg<MutableList<Opptjeningsgrunnlag>>()
            grunnlag.forEach {
                it.pia = it.pi - 50000
                it.pp = 5.5
            }
            grunnlag
        }

        val updater = OpptjeningUpdater(context)
        val inntekter = listOf(
            Inntekt(inntektAar = 2022, beloep = 600000L)
        )

        val result = updater.oppdaterOpptjeningsgrunnlagFraInntekter(
            originalGrunnlagListe = emptyList(),
            inntektListe = inntekter,
            foedselsdato = LocalDate.of(1963, 1, 1)
        )

        result shouldHaveSize 1
        result[0].pi shouldBe 600000
        result[0].pia shouldBe 550000
        result[0].pp shouldBe 5.5
    }

    test("oppdaterOpptjeningsgrunnlagFraInntekter endrer ikke original grunnlag liste") {
        val context = mockk<SimulatorContext>()
        every { context.beregnPoengtallBatch(any(), any()) } returns mutableListOf()

        val updater = OpptjeningUpdater(context)
        val originalGrunnlag = listOf(
            Opptjeningsgrunnlag().apply {
                ar = 2020
                pi = 500000
            }
        )

        updater.oppdaterOpptjeningsgrunnlagFraInntekter(
            originalGrunnlagListe = originalGrunnlag,
            inntektListe = emptyList(),
            foedselsdato = LocalDate.of(1963, 1, 1)
        )

        originalGrunnlag shouldHaveSize 1
        originalGrunnlag[0].ar shouldBe 2020
        originalGrunnlag[0].pi shouldBe 500000
    }
})
