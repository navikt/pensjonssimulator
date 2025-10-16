package no.nav.pensjon.simulator.afp.offentlig.fra2025.beregning

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beregning.domain.AfpBeregningsgrunnlag
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beregning.domain.AlderForDelingstall
import no.nav.pensjon.simulator.alder.Alder
import java.time.LocalDate

class LivsvarigOffentligAfpYtelseBeregnerTest : ShouldSpec({

    context("beregn") {
        should("beregne offentlig AFP-ytelse per år avhengig av beholdning og delingstall") {
            LivsvarigOffentligAfpYtelseBeregner.beregn(4_000_000, 19.07) shouldBe 48787.97.plusOrMinus(0.1)
            LivsvarigOffentligAfpYtelseBeregner.beregn(2_500_000, 11.68) shouldBe 49785.24.plusOrMinus( 0.1)
            LivsvarigOffentligAfpYtelseBeregner.beregn(2_150_000, 28.29) shouldBe 17677.019.plusOrMinus( 0.1)
            LivsvarigOffentligAfpYtelseBeregner.beregn(1_753_213, 15.57) shouldBe 26190.84.plusOrMinus( 0.1)
        }

        context("beregnYtelser") {
            should("beregne livsvarig offentlig AFP-ytelser for 62 år gamle brukere ved uttak") {
                val grunnlag = listOf(
                    AfpBeregningsgrunnlag(
                        pensjonsbeholdning = 3_000_000,
                        alderForDelingstall = AlderForDelingstall(
                            alder = Alder(62, 0),
                            datoVedAlder = LocalDate.of(2029, 1, 1)
                        ),
                        delingstall = 20.75
                    ),
                    AfpBeregningsgrunnlag(
                        pensjonsbeholdning = 3_150_000,
                        alderForDelingstall = AlderForDelingstall(
                            alder = Alder(63, 0),
                            datoVedAlder = LocalDate.of(2030, 1, 1)
                        ),
                        delingstall = 19.93
                    )
                )

                val resultat = LivsvarigOffentligAfpYtelseBeregner.beregnYtelser(grunnlag)

                resultat shouldHaveSize 2
                resultat[0].afpYtelsePerAar shouldBe 33628.43.plusOrMinus( 0.1)
                resultat[1].afpYtelsePerAar shouldBe 35379.03.plusOrMinus( 0.1)
            }

            should("beregne livsvarig offentlig AFP-ytelser for brukere eldre enn 62 år") {
                val grunnlag = listOf(
                    AfpBeregningsgrunnlag(
                        pensjonsbeholdning = 2_658_000,
                        alderForDelingstall = AlderForDelingstall(
                            alder = Alder(64, 2),
                            datoVedAlder = LocalDate.of(2045, 6, 3)
                        ),
                        delingstall = 20.88
                    ),
                )

                val resultat = LivsvarigOffentligAfpYtelseBeregner.beregnYtelser(grunnlag)

                resultat shouldHaveSize 1
                resultat[0].afpYtelsePerAar shouldBe 29609.29.plusOrMinus( 0.1)
            }
        }
    }
})