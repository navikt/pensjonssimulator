package no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.client.acl

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.afp.offentlig.fra2025.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.SimulerLivsvarigOffentligAfpBeholdningsperiode
import no.nav.pensjon.simulator.inntekt.Inntekt
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

class SimulerLivsvarigOffentligAfpBeholdningsgrunnlagMapperTest : ShouldSpec({

    context("toDto") {
        should("map PID, f.o.m.-dato and income periods") {
            val spec = LivsvarigOffentligAfpSpec(
                pid = Pid("12345678910"),
                foedselsdato = LocalDate.now(),
                fom = LocalDate.of(2025, 3, 1),
                fremtidigInntektListe = listOf(
                    Inntekt(fom = LocalDate.of(2025, 10, 1), aarligBeloep = 500000),
                    Inntekt(fom = LocalDate.of(2026, 10, 1), aarligBeloep = 520000),
                )
            )

            SimulerLivsvarigOffentligAfpBeholdningsgrunnlagMapper.toDto(spec) shouldBe
                    SimulerLivsvarigOffentligAfpBeholdningsgrunnlagSpec(
                        personId = "12345678910",
                        uttaksDato = LocalDate.of(2025, 3, 1),
                        fremtidigInntektListe = listOf(
                            Inntektsperiode(
                                fraOgMedDato = LocalDate.of(2025, 10, 1),
                                arligInntekt = 500_000
                            ),
                            Inntektsperiode(
                                fraOgMedDato = LocalDate.of(2026, 10, 1),
                                arligInntekt = 520_000
                            )
                        )
                    )
        }

        should("handle empty income list") {
            val spec = LivsvarigOffentligAfpSpec(
                pid = Pid("12345678910"),
                foedselsdato = LocalDate.now(),
                fom = LocalDate.of(2025, 3, 1),
                fremtidigInntektListe = emptyList()
            )

            SimulerLivsvarigOffentligAfpBeholdningsgrunnlagMapper.toDto(spec) shouldBe
                    SimulerLivsvarigOffentligAfpBeholdningsgrunnlagSpec(
                        personId = "12345678910",
                        uttaksDato = LocalDate.of(2025, 3, 1),
                        fremtidigInntektListe = emptyList()
                    )
        }
    }

    context("fromDto") {
        should("map response periods to domain periods") {
            val result = SimulerLivsvarigOffentligAfpBeholdningsgrunnlagResult(
                afpBeholdningsgrunnlag = listOf(
                    Beholdningsperiode(belop = 1000, fraOgMedDato = LocalDate.of(2025, 3, 1)),
                    Beholdningsperiode(belop = 2000, fraOgMedDato = LocalDate.of(2026, 3, 1)),
                )
            )

            SimulerLivsvarigOffentligAfpBeholdningsgrunnlagMapper.fromDto(result) shouldBe
                    listOf(
                        SimulerLivsvarigOffentligAfpBeholdningsperiode(
                            pensjonsbeholdning = 1000,
                            fom = LocalDate.of(2025, 3, 1)
                        ),
                        SimulerLivsvarigOffentligAfpBeholdningsperiode(
                            pensjonsbeholdning = 2000,
                            fom = LocalDate.of(2026, 3, 1)
                        )
                    )
        }

        should("handle empty response list") {
            val result = SimulerLivsvarigOffentligAfpBeholdningsgrunnlagResult(
                afpBeholdningsgrunnlag = emptyList()
            )

            SimulerLivsvarigOffentligAfpBeholdningsgrunnlagMapper.fromDto(result).shouldBeEmpty()
        }
    }
})