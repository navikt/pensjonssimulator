package no.nav.pensjon.simulator.opptjening.client.popp.acl.inntekt

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.inntekt.LoependeInntekt
import java.time.LocalDate

class PoppOpptjeningsgrunnlagResultTest : ShouldSpec({

    context("toInternalValue") {
        should("plukke ut seneste 'sum pensjonsgivende inntekt' og sette dato 1. januar") {
            PoppOpptjeningsgrunnlagResult(
                opptjeningsGrunnlag = PoppOpptjeningsgrunnlag(
                    inntektListe = listOf(
                        PoppInntekt(
                            inntektType = "AI",
                            inntektAr = 2023,
                            belop = 1
                        ),
                        PoppInntekt(
                            inntektType = "SUM_PI",
                            inntektAr = 2023,
                            belop = 3
                        ),
                        PoppInntekt(
                            inntektType = "SUM_PI",
                            inntektAr = 2022,
                            belop = 2
                        )
                    )
                )
            ).toInternalValue() shouldBe LoependeInntekt(
                aarligBeloep = 3,
                fom = LocalDate.of(2023, 1, 1)
            )
        }
    }
})