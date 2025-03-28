package no.nav.pensjon.simulator.beholdning

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.generelt.Person
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import no.nav.pensjon.simulator.vedtak.VedtakStatus
import no.nav.pensjon.simulator.vedtak.VedtakService
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import java.time.LocalDate

class FolketrygdBeholdningServiceTest : FunSpec({

    test("simulerFolketrygdBeholdning bruker 1. i denne/neste måned for uttak") {
        val simulator = mock(SimulatorCore::class.java)
        val vedtakService = mock(VedtakService::class.java)
        val generelleDataHolder = mock(GenerelleDataHolder::class.java)
        val simuleringSpec = simuleringSpec() // foersteUttakDato = uttakFom = 2030-02-01
        `when`(simulator.simuler(any())).thenReturn(SimulatorOutput())
        `when`(vedtakService.vedtakStatus(pid, LocalDate.of(2030, 2, 1))).thenReturn(
            VedtakStatus(harGjeldendeVedtak = false, harGjenlevenderettighet = false)
        )
        `when`(generelleDataHolder.getPerson(pid)).thenReturn(
            Person(foedselDato = LocalDate.of(1965, 6, 7), statsborgerskap = LandkodeEnum.NOR)
        )

        FolketrygdBeholdningService(simulator, vedtakService, generelleDataHolder).simulerFolketrygdBeholdning(
            FolketrygdBeholdningSpec(
                pid = pid,
                uttakFom = LocalDate.of(2030, 1, 2), // skal bli 2030-02-01
                fremtidigInntektListe = listOf(
                    InntektSpec(
                        inntektAarligBeloep = 20000,
                        inntektFom = LocalDate.of(2025, 1, 1),
                    ),
                    InntektSpec(
                        inntektAarligBeloep = 10000,
                        inntektFom = LocalDate.of(2027, 8, 1),
                    )
                ),
                antallAarUtenlandsEtter16Aar = 1,
                epsHarPensjon = true,
                epsHarInntektOver2G = false
            )
        )

        verify(simulator).simuler(simuleringSpec)
    }

    test("simulerFolketrygdBeholdning gir feilmelding for inntekt som ikke starter 1. i måneden") {
        val exception = shouldThrow<BadRequestException> {
            simulerFolketrygdBeholdning(
                listOf(
                    InntektSpec(
                        inntektAarligBeloep = 10000,
                        inntektFom = LocalDate.of(2027, 1, 2),
                    )
                )
            )
        }

        exception.message shouldBe "En fremtidig inntekt har f.o.m.-dato som ikke er den 1. i måneden"
    }

    test("simulerFolketrygdBeholdning gir feilmelding for inntekter med ikke-unik f.o.m.-dato") {
        val exception = shouldThrow<BadRequestException> {
            simulerFolketrygdBeholdning(
                listOf(
                    InntektSpec(
                        inntektAarligBeloep = 20000,
                        inntektFom = LocalDate.of(2025, 1, 1),
                    ),
                    InntektSpec(
                        inntektAarligBeloep = 10000,
                        inntektFom = LocalDate.of(2025, 1, 1), // samme fom som forrige
                    )
                )
            )
        }

        exception.message shouldBe "To fremtidige inntekter har samme f.o.m.-dato"
    }

    test("simulerFolketrygdBeholdning gir feilmelding for negativ inntekt") {
        val exception = shouldThrow<BadRequestException> {
            simulerFolketrygdBeholdning(
                listOf(
                    InntektSpec(
                        inntektAarligBeloep = -1,
                        inntektFom = LocalDate.of(2027, 1, 1),
                    )
                )
            )
        }

        exception.message shouldBe "En fremtidig inntekt har negativt beløp"
    }
})

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
        type = SimuleringType.ALDER,
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


private fun simulerFolketrygdBeholdning(inntektSpecListe: List<InntektSpec>): FolketrygdBeholdning {
    val simulator = mock(SimulatorCore::class.java)
    val vedtakService = mock(VedtakService::class.java)
    val generelleDataHolder = mock(GenerelleDataHolder::class.java)

    return FolketrygdBeholdningService(simulator, vedtakService, generelleDataHolder).simulerFolketrygdBeholdning(
        FolketrygdBeholdningSpec(
            pid = Pid("12906498357"),
            uttakFom = LocalDate.of(2031, 1, 1),
            fremtidigInntektListe = inntektSpecListe,
            antallAarUtenlandsEtter16Aar = 0,
            epsHarPensjon = false,
            epsHarInntektOver2G = false
        )
    )
}
