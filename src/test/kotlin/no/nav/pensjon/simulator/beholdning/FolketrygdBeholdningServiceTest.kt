package no.nav.pensjon.simulator.beholdning

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.exception.BadSpecException
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import no.nav.pensjon.simulator.uttak.UttaksdatoValidator
import no.nav.pensjon.simulator.vedtak.VedtakService
import no.nav.pensjon.simulator.vedtak.VedtakStatus
import java.time.LocalDate

class FolketrygdBeholdningServiceTest : FunSpec({

    test("simulerFolketrygdBeholdning bruker 1. i denne/neste måned for uttak") {
        val foedselsdato = LocalDate.of(1965, 6, 7)
        val simulator = arrangeSimulator()

        FolketrygdBeholdningService(
            simulator,
            vedtakService = arrangeVedtak(),
            personService = Arrange.foedselsdato(foedselsdato),
            time = { LocalDate.of(2025, 1, 1) },
            validator = mockk(relaxed = true),
        ).simulerFolketrygdBeholdning(
            spec = beholdningSpec(uttakFom = LocalDate.of(2030, 1, 2)) // skal bli 2030-02-01
        )

        verify { simulator.simuler(simuleringSpec()) } // foersteUttakDato = 2030-02-01
    }

    test("simulerFolketrygdBeholdning gir feilmelding for inntekt som ikke starter 1. i måneden") {
        val exception = shouldThrow<BadRequestException> {
            simulerFolketrygdBeholdning(
                listOf(
                    InntektSpec(
                        inntektAarligBeloep = 10000,
                        inntektFom = LocalDate.of(2027, 1, 2)
                    )
                )
            )
        }

        exception.message shouldBe "En fremtidig inntekt har f.o.m.-dato som ikke er den 1. i måneden"
    }

    test("simulerFolketrygdBeholdning gir feilmelding for inntekter med ikke-unik f.o.m.-dato") {
        shouldThrow<BadRequestException> {
            simulerFolketrygdBeholdning(
                listOf(
                    InntektSpec(
                        inntektAarligBeloep = 20000,
                        inntektFom = LocalDate.of(2025, 1, 1)
                    ),
                    InntektSpec(
                        inntektAarligBeloep = 10000,
                        inntektFom = LocalDate.of(2025, 1, 1) // samme f.o.m.-dato som forrige
                    )
                )
            )
        }.message shouldBe "To fremtidige inntekter har samme f.o.m.-dato"
    }

    test("simulerFolketrygdBeholdning gir feilmelding for negativ inntekt") {
        shouldThrow<BadRequestException> {
            simulerFolketrygdBeholdning(
                listOf(
                    InntektSpec(
                        inntektAarligBeloep = -1,
                        inntektFom = LocalDate.of(2027, 1, 1)
                    )
                )
            )
        }.message shouldBe "En fremtidig inntekt har negativt beløp"
    }

    test("simulerFolketrygdBeholdning validerer spesifikasjonen") {
        shouldThrow<BadSpecException> {
            FolketrygdBeholdningService(
                simulator = arrangeSimulator(),
                vedtakService = arrangeVedtak(),
                personService = Arrange.foedselsdato(LocalDate.of(1965, 6, 7)),
                time = { LocalDate.of(2025, 1, 1) },
                validator = arrangeBadSpec() // "feil" i spesifikasjonen
            ).simulerFolketrygdBeholdning(
                beholdningSpec(uttakFom = LocalDate.of(2030, 1, 1))
            )
        }.message shouldBe "feil i spesifikasjonen"
    }
})

private fun arrangeSimulator(): SimulatorCore =
    mockk<SimulatorCore>().apply {
        every { simuler(any()) } returns SimulatorOutput()
    }

private fun arrangeVedtak(): VedtakService =
    mockk<VedtakService>().apply {
        every { vedtakStatus(pid, uttakFom = LocalDate.of(2030, 2, 1)) } returns
                VedtakStatus(harGjeldendeVedtak = false, harGjenlevenderettighet = false)
    }

private fun arrangeBadSpec(): UttaksdatoValidator =
    mockk<UttaksdatoValidator>().apply {
        every { verifyUttakFom(LocalDate.of(2030, 1, 1), LocalDate.of(1965, 6, 7)) } throws             BadSpecException("feil i spesifikasjonen")
    }

private fun beholdningSpec(uttakFom: LocalDate) =
    FolketrygdBeholdningSpec(
        pid = pid,
        uttakFom,
        fremtidigInntektListe = listOf(
            InntektSpec(
                inntektAarligBeloep = 20000,
                inntektFom = LocalDate.of(2025, 1, 1)
            ),
            InntektSpec(
                inntektAarligBeloep = 10000,
                inntektFom = LocalDate.of(2027, 8, 1)
            )
        ),
        antallAarUtenlandsEtter16Aar = 1,
        epsHarPensjon = true,
        epsHarInntektOver2G = false
    )

private fun simuleringSpec() =
    SimuleringSpec(
        pid = pid,
        foersteUttakDato = LocalDate.of(2030, 2, 1),
        uttakGrad = UttakGradKode.P_100,
        heltUttakDato = null,
        utlandAntallAar = 1,
        sivilstatus = SivilstatusType.GIFT,
        epsHarPensjon = true,
        epsHarInntektOver2G = false,
        fremtidigInntektListe = mutableListOf(
            FremtidigInntekt(
                aarligInntektBeloep = 20000,
                fom = LocalDate.of(2025, 1, 1)
            ),
            FremtidigInntekt(
                aarligInntektBeloep = 10000,
                fom = LocalDate.of(2027, 8, 1)
            )
        ),
        brukFremtidigInntekt = true,
        type = SimuleringTypeEnum.ALDER,
        foedselAar = 0, // only for anonym
        forventetInntektBeloep = 0, // inntekt instead given by fremtidigInntektListe
        inntektOver1GAntallAar = 0, // only for anonym
        inntektUnderGradertUttakBeloep = 0, // inntekt instead given by fremtidigInntektListe
        inntektEtterHeltUttakBeloep = 0, // inntekt instead given by fremtidigInntektListe
        inntektEtterHeltUttakAntallAar = 0, // inntekt instead given by fremtidigInntektListe
        foedselDato = LocalDate.of(1965, 6, 7),
        avdoed = null,
        isTpOrigSimulering = true,
        simulerForTp = false,
        flyktning = null,
        utlandPeriodeListe = mutableListOf(),
        rettTilOffentligAfpFom = null,
        pre2025OffentligAfp = null,
        erAnonym = false,
        ignoreAvslag = true, // true for folketrygdbeholdning
        isHentPensjonsbeholdninger = true,
        isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true,
        onlyVilkaarsproeving = false,
        epsKanOverskrives = false
    )

private fun simulerFolketrygdBeholdning(inntektSpecListe: List<InntektSpec>): FolketrygdBeholdning =
    FolketrygdBeholdningService(
        simulator = mockk<SimulatorCore>(),
        time = { LocalDate.of(2025, 1, 1) },
        vedtakService = mockk<VedtakService>(),
        personService = mockk<GeneralPersonService>(),
        validator = mockk<UttaksdatoValidator>()
    ).simulerFolketrygdBeholdning(
        FolketrygdBeholdningSpec(
            pid = Pid("12906498357"),
            uttakFom = LocalDate.of(2031, 1, 1),
            fremtidigInntektListe = inntektSpecListe,
            antallAarUtenlandsEtter16Aar = 0,
            epsHarPensjon = false,
            epsHarInntektOver2G = false
        )
    )
