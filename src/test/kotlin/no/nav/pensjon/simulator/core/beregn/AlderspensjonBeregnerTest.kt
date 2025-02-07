package no.nav.pensjon.simulator.core.beregn

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.DelingstallUtvalg
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForholdstallUtvalg
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.GarantitilleggsbeholdningGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.exception.BadSpecException
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import no.nav.pensjon.simulator.testutil.TestObjects
import org.mockito.Mockito.mock
import java.time.LocalDate
import java.util.Calendar

class AlderspensjonBeregnerTest : FunSpec({

    /*
    test("beregnAlderspensjon throws exception when revurdering and 100% uttak fom = virkningdato") {
        val exception = shouldThrow<BadSpecException> {
            AlderspensjonBeregner(mock(SimulatorContext::class.java)).beregnAlderspensjon(
                kravhode = Kravhode().apply {
                    uttaksgradListe = mutableListOf(
                        Uttaksgrad().apply {
                            uttaksgrad = 100
                            fomDato = dateAtNoon(2025, Calendar.MARCH, 1)
                        })
                },
                vedtakListe = mutableListOf(),
                virkningDato = LocalDate.of(2025, 3, 1), // = Uttaksgrad fomDato
                forholdstallUtvalg = ForholdstallUtvalg(),
                delingstallUtvalg = DelingstallUtvalg(),
                sisteAldersberegning2011 = null,
                privatAfp = null,
                garantitilleggBeholdningGrunnlag = GarantitilleggsbeholdningGrunnlag(),
                simuleringSpec = TestObjects.simuleringSpec,
                sakId = null,
                isFoersteUttak = false, // i.e. revurdering
                ignoreAvslag = false
            )
        }

        exception.message shouldBe "uttaksdato er for tidlig (personen har eksisterende alderpensjonsvedtak)"
    }*/
})
