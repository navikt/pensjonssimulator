package no.nav.pensjon.simulator.alderspensjon.alternativ

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligOpptjeningException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligTrygdetidException
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.normalder.NormAlderService
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDate

class UfoereAlternativSimuleringServiceTest : FunSpec({

    test("simulerMedFallendeUttaksgrad for gradert uttak should return: alternativ med lavere grad men samme uttaksdato") {
        val simulator = mock(SimulatorCore::class.java).also { arrangeSimulatorForGradertUttak(it) }
        val normAlderService = mock(NormAlderService::class.java).also { arrangeNormAlder(it) }

        val service = UfoereAlternativSimuleringService(
            simulator, normAlderService, mock(UfoereAlternativtUttakService::class.java)
        )

        service.simulerMedFallendeUttaksgrad(
            spec = simuleringSpec(
                foersteUttakDato = LocalDate.of(2030, 2, 1),
                uttaksgrad = UttakGradKode.P_60,
                heltUttakDato = LocalDate.of(2032, 2, 1)
            )
        ).alternativ shouldBe SimulertAlternativ(
            gradertUttakAlder = SimulertUttakAlder(alder = Alder(63, 0), uttakDato = LocalDate.of(2030, 2, 1)),
            uttakGrad = UttakGradKode.P_40,
            heltUttakAlder = SimulertUttakAlder(alder = Alder(65, 0), uttakDato = LocalDate.of(2032, 2, 1)),
            resultStatus = SimulatorResultStatus.GOOD
        )
    }

    test("simulerMedFallendeUttaksgrad for helt uttak should return: alternativ med lavere grad med helt uttak-dato som ny gradert uttak-dato") {
        val simulator = mock(SimulatorCore::class.java).also { arrangeSimulatorForHeltUttak(it) }
        val normAlderService = mock(NormAlderService::class.java).also { arrangeNormAlder(it) }

        val service = UfoereAlternativSimuleringService(
            simulator, normAlderService, mock(UfoereAlternativtUttakService::class.java)
        )

        service.simulerMedFallendeUttaksgrad(
            spec = simuleringSpec(
                foersteUttakDato = LocalDate.of(2030, 2, 1),
                uttaksgrad = UttakGradKode.P_100,
                heltUttakDato = null // brukes bare ved gradert uttak (etterfulgt av helt uttak)
            )
        ).alternativ shouldBe SimulertAlternativ(
            gradertUttakAlder = SimulertUttakAlder(alder = Alder(63, 0), uttakDato = LocalDate.of(2030, 2, 1)),
            uttakGrad = UttakGradKode.P_60,
            heltUttakAlder = SimulertUttakAlder(alder = Alder(67, 0), uttakDato = LocalDate.of(2034, 2, 1)),
            resultStatus = SimulatorResultStatus.GOOD
        )
    }

    test("simulerMedFallendeUttaksgrad for ingen innvilgede uttak should throw exception") {
        val simulator = mock(SimulatorCore::class.java).also { arrangeSimulatorForIngenInnvilgedeUttak(it) }
        val normAlderService = mock(NormAlderService::class.java).also { arrangeNormAlder(it) }

        val service = UfoereAlternativSimuleringService(
            simulator, normAlderService, mock(UfoereAlternativtUttakService::class.java)
        )

        shouldThrow<UtilstrekkeligOpptjeningException> {
            service.simulerMedFallendeUttaksgrad(
                spec = simuleringSpec(
                    foersteUttakDato = LocalDate.of(2030, 2, 1),
                    uttaksgrad = UttakGradKode.P_40,
                    heltUttakDato = LocalDate.of(2032, 2, 1)
                )
            )
        }.message shouldBe null
    }
})

private fun arrangeNormAlder(service: NormAlderService) {
    `when`(service.normAlder(foedselsdato = LocalDate.of(1967, 1, 1))).thenReturn(Alder(67, 0))
}

private fun arrangeSimulatorForGradertUttak(simulator: SimulatorCore) {
    `when`(simulator.fetchFoedselsdato(pid)).thenReturn(LocalDate.of(1967, 1, 1))

    // Arrange a series of simulations that converge towards the best alternative:
    `when`(
        simulator.simuler(
            simuleringSpec(
                foersteUttakDato = LocalDate.of(2030, 2, 1),
                uttaksgrad = UttakGradKode.P_50,
                heltUttakDato = LocalDate.of(2032, 2, 1)
            )
        )
    ).thenThrow(UtilstrekkeligOpptjeningException())

    `when`(
        simulator.simuler(
            simuleringSpec(
                foersteUttakDato = LocalDate.of(2030, 2, 1),
                uttaksgrad = UttakGradKode.P_40,
                heltUttakDato = LocalDate.of(2032, 2, 1)
            )
        )
    ).thenReturn(SimulatorOutput())
}

private fun arrangeSimulatorForHeltUttak(simulator: SimulatorCore) {
    `when`(simulator.fetchFoedselsdato(pid)).thenReturn(LocalDate.of(1967, 1, 1))

    // Arrange a series of simulations that converge towards the best alternative:
    `when`(
        simulator.simuler(
            simuleringSpec(
                foersteUttakDato = LocalDate.of(2030, 2, 1),
                uttaksgrad = UttakGradKode.P_80,
                heltUttakDato = LocalDate.of(2034, 2, 1) // normert pensjonsalder
            )
        )
    ).thenThrow(UtilstrekkeligOpptjeningException())

    `when`(
        simulator.simuler(
            simuleringSpec(
                foersteUttakDato = LocalDate.of(2030, 2, 1),
                uttaksgrad = UttakGradKode.P_60,
                heltUttakDato = LocalDate.of(2034, 2, 1) // normert pensjonsalder
            )
        )
    ).thenReturn(SimulatorOutput())
}

private fun arrangeSimulatorForIngenInnvilgedeUttak(simulator: SimulatorCore) {
    `when`(simulator.fetchFoedselsdato(pid)).thenReturn(LocalDate.of(1967, 1, 1))

    `when`(
        simulator.simuler(
            simuleringSpec(
                foersteUttakDato = LocalDate.of(2030, 2, 1),
                uttaksgrad = UttakGradKode.P_40,
                heltUttakDato = LocalDate.of(2032, 2, 1)
            )
        )
    ).thenThrow(UtilstrekkeligTrygdetidException())

    `when`(
        simulator.simuler(
            simuleringSpec(
                foersteUttakDato = LocalDate.of(2030, 2, 1),
                uttaksgrad = UttakGradKode.P_20,
                heltUttakDato = LocalDate.of(2032, 2, 1)
            )
        )
    ).thenThrow(UtilstrekkeligOpptjeningException())
}

/**
 * IndexBasedSimulering.tryIndex: discriminator.simuler(indexSimulatorSpec)
 */
private fun simuleringSpec(foersteUttakDato: LocalDate, uttaksgrad: UttakGradKode, heltUttakDato: LocalDate?) =
    SimuleringSpec(
        type = SimuleringType.ALDER_M_AFP_PRIVAT,
        sivilstatus = SivilstatusType.UGIF,
        epsHarPensjon = false,
        foersteUttakDato = foersteUttakDato,
        heltUttakDato = heltUttakDato,
        pid = pid,
        foedselDato = LocalDate.of(1967, 1, 1),
        avdoed = null,
        isTpOrigSimulering = false,
        simulerForTp = false,
        uttakGrad = uttaksgrad,
        forventetInntektBeloep = 250000,
        inntektUnderGradertUttakBeloep = 125000,
        inntektEtterHeltUttakBeloep = 67500,
        inntektEtterHeltUttakAntallAar = null,
        foedselAar = 1967,
        utlandAntallAar = 3,
        utlandPeriodeListe = mutableListOf(),
        fremtidigInntektListe = mutableListOf(),
        brukFremtidigInntekt = true,
        inntektOver1GAntallAar = 0,
        flyktning = false,
        epsHarInntektOver2G = true,
        rettTilOffentligAfpFom = null,
        pre2025OffentligAfp = null,
        erAnonym = false,
        ignoreAvslag = false,
        isHentPensjonsbeholdninger = true,
        isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
        onlyVilkaarsproeving = false,
        epsKanOverskrives = false
    )
