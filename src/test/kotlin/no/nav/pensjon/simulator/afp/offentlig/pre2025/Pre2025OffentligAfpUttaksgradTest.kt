package no.nav.pensjon.simulator.afp.offentlig.pre2025

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2016
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.krav.KravService
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import no.nav.pensjon.simulator.validity.BadSpecException
import java.time.LocalDate
import java.util.*

class Pre2025OffentligAfpUttaksgradTest : ShouldSpec({

    /**
     * Tester at uttaksgrad-listen ved førstegangsuttak inneholder 100 % grad f.o.m. 1. dag i måneden etter at
     * søker oppnår normert pensjonsalder. Uttaksgraden skal være tidsubegrenset.
     * --------------
     * Med fødselsdato 1963-01-15 og normalder 67 år, så oppnås normalder 2030-01-15.
     * 1. dag i måneden etter dette er 2030-02-01.
     */
    context("førstegangsuttak") {
        should("uttaksgradliste inneholde 100 % uttaksgrad") {
            val uttaksgradListe = Pre2025OffentligAfpUttaksgrad(
                kravService = mockk(),
                normalderService = Arrange.normalder(foedselsdato = LocalDate.of(1963, 1, 15))
            ).uttaksgradListe(
                spec = simuleringSpec,
                forrigeAlderspensjonBeregningResultat = null, // => førstegangsuttak
                foedselsdato = LocalDate.of(1963, 1, 15)
            )

            uttaksgradListe.size shouldBe 1
            with(uttaksgradListe[0]) {
                fomDato shouldBe dateAtNoon(2030, Calendar.FEBRUARY, 1)
                tomDato shouldBe null // => tidsubegrenset
                uttaksgrad shouldBe 100
            }
        }
    }

    context("AFP-perioden overlapper alderspensjonsperioden") {
        should("gi beskrivende feilmelding") {
            // AFP-perioden overlapper alderspensjonsperioden med 1 måned:
            val startdatoForAfp = historiskAlderspensjonTom.minusMonths(1).plusDays(1)
            val loepende0Uttak: Uttaksgrad = loepende0Uttak(fom = startdatoForAfp)
            val foedselsdato = LocalDate.of(1962, 1, 15)

            shouldThrow<BadSpecException> {
                Pre2025OffentligAfpUttaksgrad(
                    kravService = arrangeKrav(loepende0Uttak),
                    normalderService = Arrange.normalder(foedselsdato)
                ).uttaksgradListe(
                    spec = simuleringSpec(foersteUttakDato = startdatoForAfp),
                    forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2016().apply { kravId = 1L },
                    foedselsdato
                )
            }.message shouldBe
                    "For tidlig uttak av AFP (må starte etter alderspensjonsperiodens slutt, dvs. tidligst 2026-03-01)"
        }
    }

    context("løpende 0-uttak fra samme dato som startdato for AFP") {
        should("uttaksgradliste inneholde 0 % uttaksgrad") {
            val startdatoForAfp = historiskAlderspensjonTom.plusDays(1)
            val loepende0Uttak: Uttaksgrad = loepende0Uttak(fom = startdatoForAfp)
            val foedselsdato = LocalDate.of(1962, 1, 15)
            // Med normalder 67 år 0 måneder blir ubetinget uttaksdato 2029-02-01

            val uttaksgradListe = Pre2025OffentligAfpUttaksgrad(
                kravService = arrangeKrav(loepende0Uttak),
                normalderService = Arrange.normalder(foedselsdato) // normalder 67 år 0 måneder
            ).uttaksgradListe(
                spec = simuleringSpec(foersteUttakDato = startdatoForAfp),
                forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2016().apply { kravId = 1L },
                foedselsdato
            )

            uttaksgradListe.size shouldBe 3
            with(uttaksgradListe[0]) {
                // Terminert alderspensjonsperiode:
                fomDato shouldBe LocalDate.of(2024, 1, 1).toNorwegianDateAtNoon()
                tomDato shouldBe historiskAlderspensjonTom.toNorwegianDateAtNoon()
                uttaksgrad shouldBe 100
            }
            with(uttaksgradListe[1]) {
                // AFP-periode:
                fomDato shouldBe LocalDate.of(2026, 3, 1).toNorwegianDateAtNoon()
                tomDato shouldBe LocalDate.of(2029, 1, 31).toNorwegianDateAtNoon()
                uttaksgrad shouldBe 0
            }
            with(uttaksgradListe[2]) {
                // Alderspensjon etter AFP, f.o.m. ubetinget uttaksdato:
                fomDato shouldBe LocalDate.of(2029, 2, 1).toNorwegianDateAtNoon()
                tomDato shouldBe null // => tidsubegrenset
                uttaksgrad shouldBe 100
            }
        }
    }

    context("løpende 0-uttak fra måneden før startdato for AFP, med gap mellom historisk alderspensjon og AFP-perioden") {
        should("uttaksgradlisten være kontinuerlig") {
            // 1 måneds gap mellom historisk alderspensjon og AFP-perioden:
            val startdatoForAfp = historiskAlderspensjonTom.plusMonths(1).plusDays(1)
            val loepende0Uttak: Uttaksgrad = loepende0Uttak(fom = startdatoForAfp.minusMonths(1))
            val foedselsdato = LocalDate.of(1962, 1, 15)
            // Med normalder 67 år 0 måneder blir ubetinget uttaksdato 2029-02-01

            val uttaksgradListe = Pre2025OffentligAfpUttaksgrad(
                kravService = arrangeKrav(loepende0Uttak),
                normalderService = Arrange.normalder(foedselsdato) // normalder 67 år 0 måneder
            ).uttaksgradListe(
                spec = simuleringSpec(foersteUttakDato = startdatoForAfp),
                forrigeAlderspensjonBeregningResultat = BeregningsResultatAlderspensjon2016().apply { kravId = 1L },
                foedselsdato
            )

            uttaksgradListe.size shouldBe 3
            with(uttaksgradListe[0]) {
                // Terminert alderspensjonsperiode:
                fomDato shouldBe LocalDate.of(2024, 1, 1).toNorwegianDateAtNoon()
                tomDato shouldBe historiskAlderspensjonTom.toNorwegianDateAtNoon()
                uttaksgrad shouldBe 100
            }
            with(uttaksgradListe[1]) {
                // AFP-periode:
                fomDato shouldBe LocalDate.of(2026, 3, 1).toNorwegianDateAtNoon()
                tomDato shouldBe LocalDate.of(2029, 1, 31).toNorwegianDateAtNoon()
                uttaksgrad shouldBe 0
            }
            with(uttaksgradListe[2]) {
                // Alderspensjon etter AFP, f.o.m. ubetinget uttaksdato:
                fomDato shouldBe LocalDate.of(2029, 2, 1).toNorwegianDateAtNoon()
                tomDato shouldBe null // => tidsubegrenset
                uttaksgrad shouldBe 100
            }
        }
    }
})

private val historiskAlderspensjonTom = LocalDate.of(2026, 2, 28)

private fun arrangeKrav(loepende0Uttak: Uttaksgrad): KravService =
    mockk<KravService>().apply {
        every { fetchKravhode(any()) } returns Kravhode().apply {
            uttaksgradListe = mutableListOf(
                Uttaksgrad().apply {
                    uttaksgrad = 100
                    fomDato = LocalDate.of(2024, 1, 1).toNorwegianDateAtNoon()
                    tomDato = historiskAlderspensjonTom.toNorwegianDateAtNoon()
                },
                loepende0Uttak
            )
        }
    }

private fun loepende0Uttak(fom: LocalDate) =
    Uttaksgrad().apply {
        uttaksgrad = 0
        fomDato = fom.toNorwegianDateAtNoon()
        tomDato = null
    }
