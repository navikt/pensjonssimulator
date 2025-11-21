package no.nav.pensjon.simulator.afp.offentlig.fra2025.beregning

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.afp.offentlig.fra2025.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.SimulerLivsvarigOffentligAfpBeholdningsgrunnlagClient
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.SimulerLivsvarigOffentligAfpBeholdningsperiode
import no.nav.pensjon.simulator.afp.offentlig.fra2025.grunnlag.LivsvarigOffentligAfpResult
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.sats.Delingstall
import no.nav.pensjon.simulator.core.domain.regler.to.HentDelingstallResponse
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

class LivsvarigOffentligAfpBeregningServiceTest : ShouldSpec({

    should("simulere beregning av offentlig AFP med uttak ved 62 år") {
        val afpBeholdningGrunnlagResponse = listOf(
            SimulerLivsvarigOffentligAfpBeholdningsperiode(
                pensjonsbeholdning = 5441510,
                fom = LocalDate.of(2026, 1, 1)
            ),
            SimulerLivsvarigOffentligAfpBeholdningsperiode(
                pensjonsbeholdning = 5513910,
                fom = LocalDate.of(2027, 1, 1)
            )
        )
        val foedselsdato = LocalDate.of(1964, 11, 7)
        val lavesteAlderVedUttak = Alder(aar = 62, maaneder = 0)
        val alderVedAarsskifte = Alder(aar = 62, maaneder = 1)

        val service = LivsvarigOffentligAfpBeregningService(
            afpBeholdningClient = arrangeAfpBeholdning(afpBeholdningGrunnlagResponse),
            simulatorContext = arrangeDelingstall(foedselsdato, lavesteAlderVedUttak, alderVedAarsskifte)
        )

        val resultat: LivsvarigOffentligAfpResult = service.simuler(
            LivsvarigOffentligAfpSpec(
                pid = Pid("07516443469"),
                foedselsdato,
                fom = LocalDate.of(2026, 12, 1),
                fremtidigInntektListe = emptyList()
            )
        )

        with(resultat) {
            afpYtelseListe shouldHaveSize 2
            with(afpYtelseListe[0]) {
                afpYtelsePerAar shouldBe 62134.38.plusOrMinus(0.01)
                gjelderFom shouldBe LocalDate.of(2026, 12, 1)
            }
            with(afpYtelseListe[1]) {
                afpYtelsePerAar shouldBe 62963.53.plusOrMinus(0.01)
                gjelderFom shouldBe LocalDate.of(2027, 1, 1)
            }
        }
    }
})

private fun arrangeAfpBeholdning(
    beholdningsperiodeListe: List<SimulerLivsvarigOffentligAfpBeholdningsperiode>
): SimulerLivsvarigOffentligAfpBeholdningsgrunnlagClient =
    mockk<SimulerLivsvarigOffentligAfpBeholdningsgrunnlagClient> {
        every { simuler(any()) } returns beholdningsperiodeListe
    }

private fun arrangeDelingstall(
    foedselsdato: LocalDate,
    lavesteAlderVedUttak: Alder,
    alderVedAarsskifte: Alder
): SimulatorContext =
    mockk<SimulatorContext> {
        every { hentDelingstall(any()) } returns
                HentDelingstallResponse(
                    arskull = foedselsdato.year,
                    delingstall = listOf(
                        Delingstall(alder = lavesteAlderVedUttak, delingstall = 20.37),
                        Delingstall(alder = alderVedAarsskifte, delingstall = 20.31)
                    )
                )
    }
