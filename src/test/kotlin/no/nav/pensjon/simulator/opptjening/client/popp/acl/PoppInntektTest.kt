package no.nav.pensjon.simulator.opptjening.client.popp.acl

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.InntekttypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Inntektsgrunnlag
import java.time.LocalDate

class PoppInntektTest : ShouldSpec({

    context("erRelevant") {
        context("inntektsår er etter siste spesialbehandlingsår") {
            should("gi 'false'") {
                PoppInntekt(inntektAr = 1968, inntektType = "AI").erRelevant() shouldBe false
            }
        }

        context("inntektstype er verken 'pensjonsgivende inntekt 1966' eller 'antatt inntekt'") {
            should("gi 'false'") {
                PoppInntekt(inntektAr = 1965, inntektType = "ARBLTO").erRelevant() shouldBe false
            }
        }

        context("inntektsår er ikke etter siste spesialbehandlingsår") {
            context("inntektstype er 'pensjonsgivende inntekt 1966'") {
                should("gi 'true'") {
                    PoppInntekt(inntektAr = 1966, inntektType = "PI66").erRelevant() shouldBe true
                }
            }

            context("inntektstype er 'antatt inntekt'") {
                should("gi 'true'") {
                    PoppInntekt(inntektAr = 1967, inntektType = "AI").erRelevant() shouldBe true
                }
            }
        }
    }

    context("toGrunnlag") {
        should("mappe til domenerepresentasjon av 'inntektsgrunnlag'") {
            val result: Inntektsgrunnlag = PoppInntekt(
                inntektAr = 1967,
                belop = 123L,
                inntektType = "PI66"
            ).toGrunnlag()

            with(result) {
                inntektTypeEnum shouldBe InntekttypeEnum.ARBLIGN // PI66 -> ARBLIGN
                fomLd shouldBe LocalDate.of(1967, 1, 1)
                tomLd shouldBe LocalDate.of(1967, 12, 31)
                belop shouldBe 123
                bruk shouldBe true // hardkodet
                erRelevant shouldBe true
            }
        }
    }
})
