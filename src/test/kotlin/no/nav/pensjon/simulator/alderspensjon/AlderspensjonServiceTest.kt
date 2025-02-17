package no.nav.pensjon.simulator.alderspensjon

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alderspensjon.alternativ.AlternativSimuleringService
import no.nav.pensjon.simulator.alderspensjon.spec.AlderspensjonSpec
import no.nav.pensjon.simulator.alderspensjon.spec.PensjonInntektSpec
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.exception.FeilISimuleringsgrunnlagetException
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.generelt.Person
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import no.nav.pensjon.simulator.vedtak.VedtakService
import no.nav.pensjon.simulator.vedtak.VedtakStatus
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDate

class AlderspensjonServiceTest : FunSpec({

    test("simulerAlderspensjon gir feilmelding for inntekt som ikke starter 1. i måneden") {
        val exception = shouldThrow<BadRequestException> {
            simulerAlderspensjon(
                inntektSpecListe = listOf(
                    PensjonInntektSpec(
                        aarligBeloep = 10000,
                        fom = LocalDate.of(2027, 1, 2),
                    )
                )
            )
        }

        exception.message shouldBe "En fremtidig inntekt har f.o.m.-dato som ikke er den 1. i måneden"
    }

    test("simulerAlderspensjon gir feilmelding for inntekter med ikke-unik f.o.m.-dato") {
        val exception = shouldThrow<BadRequestException> {
            simulerAlderspensjon(
                inntektSpecListe = listOf(
                    PensjonInntektSpec(
                        aarligBeloep = 20000,
                        fom = LocalDate.of(2025, 1, 1),
                    ),
                    PensjonInntektSpec(
                        aarligBeloep = 10000,
                        fom = LocalDate.of(2025, 1, 1), // samme fom som forrige
                    )
                )
            )
        }

        exception.message shouldBe "To fremtidige inntekter har samme f.o.m.-dato"
    }

    test("simulerAlderspensjon gir feilmelding for negativ inntekt") {
        val exception = shouldThrow<BadRequestException> {
            simulerAlderspensjon(
                inntektSpecListe = listOf(
                    PensjonInntektSpec(
                        aarligBeloep = -1,
                        fom = LocalDate.of(2027, 1, 1),
                    )
                )
            )
        }

        exception.message shouldBe "En fremtidig inntekt har negativt beløp"
    }

    test("simulerAlderspensjon gir feilmelding hvis gjenlevenderettighet") {
        val exception = shouldThrow<FeilISimuleringsgrunnlagetException> {
            simulerAlderspensjon(
                inntektSpecListe = emptyList(),
                harGjenlevenderettighet = true
            )
        }

        exception.message shouldBe "Kan ikke simulere bruker med gjenlevenderettigheter"
    }
})

private fun simulerAlderspensjon(
    inntektSpecListe: List<PensjonInntektSpec>,
    harGjenlevenderettighet: Boolean = false
): AlderspensjonResult =
    AlderspensjonService(
        simulator = mock(SimulatorCore::class.java),
        alternativSimuleringService = mock(AlternativSimuleringService::class.java),
        vedtakService = vedtakService(harGjenlevenderettighet),
        generelleDataHolder = generelleDataHolder()
    ).simulerAlderspensjon(
        AlderspensjonSpec(
            pid,
            gradertUttak = null,
            heltUttakFom = LocalDate.of(2027, 1, 1),
            antallAarUtenlandsEtter16 = 0,
            epsHarPensjon = false,
            epsHarInntektOver2G = false,
            fremtidigInntektListe = inntektSpecListe,
            livsvarigOffentligAfpRettFom = null
        )
    )

private fun vedtakService(harGjenlevenderettighet: Boolean): VedtakService =
    mock(VedtakService::class.java).also {
        `when`(it.vedtakStatus(pid, LocalDate.of(2027, 1, 1))).thenReturn(
            VedtakStatus(harGjeldendeVedtak = false, harGjenlevenderettighet)
        )
    }

private fun generelleDataHolder(): GenerelleDataHolder =
    mock(GenerelleDataHolder::class.java).also {
        `when`(it.getPerson(pid)).thenReturn(
            Person(foedselDato = LocalDate.of(1963, 1, 1), statsborgerskap = LandkodeEnum.NOR)
        )
    }
