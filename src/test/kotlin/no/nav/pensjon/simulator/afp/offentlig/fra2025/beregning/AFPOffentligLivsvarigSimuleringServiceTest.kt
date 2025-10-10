package no.nav.pensjon.simulator.afp.offentlig.fra2025.beregning

import no.nav.pensjon.simulator.core.domain.regler.sats.Delingstall
import no.nav.pensjon.simulator.afp.offentlig.fra2025.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.SimulerLivsvarigOffentligAfpBeholdningsgrunnlagClient
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.SimulerLivsvarigOffentligAfpBeholdningsperiode
import no.nav.pensjon.simulator.afp.offentlig.fra2025.grunnlag.LivsvarigOffentligAfpResult
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.to.HentDelingstallResponse
import no.nav.pensjon.simulator.person.Pid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import java.time.LocalDate

class AFPOffentligLivsvarigSimuleringServiceTest {

    @Test
    fun `simuler beregning av AFP Offentlig med uttak ved 62 aar`() {
        val afpBeholdningGrunnlagResponse = listOf(
            SimulerLivsvarigOffentligAfpBeholdningsperiode(5441510, LocalDate.of(2026, 1, 1)),
            SimulerLivsvarigOffentligAfpBeholdningsperiode(
                5513910, LocalDate.of(2027, 1, 1)
            )
        )
        val fodselsdato = LocalDate.of(1964, 11, 7)
        val lavesteAlderVedUttak = Alder(62, 0)
        val alderVedAarsskifte = Alder(62, 1)

        val afpBeholdningClient = mock<SimulerLivsvarigOffentligAfpBeholdningsgrunnlagClient> {
            on { simulerAfpBeholdningGrunnlag(any()) }.thenReturn(afpBeholdningGrunnlagResponse)
        }
        val delingstallClient = mock<SimulatorContext> {
            on { hentDelingstall(any()) }.thenReturn(
                HentDelingstallResponse(
                    fodselsdato.year,
                    listOf(
                        Delingstall(lavesteAlderVedUttak, 20.37),
                        Delingstall(alderVedAarsskifte, 20.31)
                    )
                )
            )
        }
        val service = LivsvarigOffentligAfpBeregningService(afpBeholdningClient, delingstallClient)

        val resultat: LivsvarigOffentligAfpResult = service.simuler(
            LivsvarigOffentligAfpSpec(
                Pid("07516443469"),
                fodselsdato,
                LocalDate.of(2026, 12, 1),
                listOf()
            )
        )
        assertEquals(2, resultat.afpYtelseListe.size)
        with(resultat.afpYtelseListe[0]) {
            assertEquals(62134.38, afpYtelsePerAar, 0.01)
            assertEquals(LocalDate.of(2026, 12, 1), gjelderFom)
        }
        with(resultat.afpYtelseListe[1]) {
            assertEquals(62963.53, afpYtelsePerAar, 0.01)
            assertEquals(LocalDate.of(2027, 1, 1), gjelderFom)
        }
    }
}
