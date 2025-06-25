package no.nav.pensjon.simulator.afp.offentlig.pre2025

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.VedtakResultatEnum
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.g.GrunnbeloepService
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestObjects.persongrunnlag
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import java.time.LocalDate

class Pre2025OffentligAfpFoerstegangBeregnerTest : FunSpec({

    test("beregnAfp skal gi innvilget vedtak hvis simulering OK") {
        Pre2025OffentligAfpFoerstegangBeregner(
            context = arrangeSimuleringsresultat(),
            normalderService = Arrange.normalder(),
            ufoereService = mockk(),
            grunnbeloepService = arrangeGrunnbeloep()
        ).beregnAfp(
            spec = simuleringSpec(afpOrdning = AFPtypeEnum.AFPSTAT), // foersteUttakDato = 2029-01-01
            kravhode = Kravhode().apply { persongrunnlagListe = mutableListOf(persongrunnlag) },
            forrigeAlderspensjonBeregningResultat = null
        ).simuleringResult?.statusEnum shouldBe VedtakResultatEnum.INNV
    }
})

private fun arrangeSimuleringsresultat(): SimulatorContext =
    mockk<SimulatorContext>().apply {
        every { simulerVilkarsprovPre2025OffentligAfp(any()) } returns Simuleringsresultat()
        every { simulerPre2025OffentligAfp(any()) } returns Simuleringsresultat()
    }

private fun arrangeGrunnbeloep(): GrunnbeloepService =
    mockk<GrunnbeloepService>().apply {
        every {
            grunnbeloep(dato = LocalDate.of(2029, 1, 1)) // virkningFom, foersteUttakDato
        } returns 123000
    }
