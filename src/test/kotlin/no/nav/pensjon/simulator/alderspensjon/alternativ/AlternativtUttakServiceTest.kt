package no.nav.pensjon.simulator.alderspensjon.alternativ

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.alder.PensjonAlderDato
import no.nav.pensjon.simulator.alderspensjon.alternativ.AlternativtUttakServiceTestObjects.arrangeSimulator
import no.nav.pensjon.simulator.alderspensjon.alternativ.AlternativtUttakServiceTestObjects.simuleringSpec
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligOpptjeningException
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.GradertUttakSimuleringSpec
import no.nav.pensjon.simulator.core.spec.HeltUttakSimuleringSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class AlternativtUttakServiceTest : FunSpec({

    test("findAlternativtUttak should find alternativt uttak") {
        val service = AlternativtUttakService(
            simulator = arrangeSimulator(),
            normalderService = Arrange.normalder(foedselsdato = LocalDate.of(1967, 1, 1)),
            time = { LocalDate.of(2025, 1, 1) }
        )

        service.findAlternativtUttak(
            spec = simuleringSpec(
                foersteUttakDato = LocalDate.of(2030, 2, 1),
                uttaksgrad = UttakGradKode.P_50,
                heltUttakDato = LocalDate.of(2032, 2, 1)
            ),
            gradertUttak = GradertUttakSimuleringSpec(
                grad = UttakGradKode.P_50,
                uttakFom = PensjonAlderDato(alder = Alder(63, 0), dato = LocalDate.of(2030, 2, 1)),
                aarligInntektBeloep = null
            ),
            heltUttak = HeltUttakSimuleringSpec(
                uttakFom = PensjonAlderDato(alder = Alder(65, 0), dato = LocalDate.of(2032, 2, 1)),
                aarligInntektBeloep = 123000,
                inntektTom = PensjonAlderDato(alder = Alder(67, 0), dato = LocalDate.of(2034, 2, 1))
            )
        ) shouldBe SimulertPensjonEllerAlternativ(
            pensjon = SimulertPensjon(
                alderspensjon = emptyList(),
                alderspensjonFraFolketrygden = emptyList(),
                privatAfp = emptyList(),
                pre2025OffentligAfp = null,
                livsvarigOffentligAfp = emptyList(),
                pensjonBeholdningPeriodeListe = emptyList(),
                harUttak = false,
                harTilstrekkeligTrygdetid = false,
                trygdetid = 0,
                opptjeningGrunnlagListe = emptyList()
            ),
            alternativ = SimulertAlternativ(
                gradertUttakAlder = SimulertUttakAlder(alder = Alder(65, 1), uttakDato = LocalDate.of(2032, 3, 1)),
                uttakGrad = UttakGradKode.P_40,
                heltUttakAlder = SimulertUttakAlder(alder = Alder(66, 1), uttakDato = LocalDate.of(2033, 3, 1)),
                resultStatus = SimulatorResultStatus.GOOD
            )
        )
    }
})

object AlternativtUttakServiceTestObjects {

    fun arrangeSimulator() =
        mockk<SimulatorCore>(relaxed = true).apply {
            every { fetchFoedselsdato(pid) } returns LocalDate.of(1967, 1, 1)

            // Arrange a series of simulations that converge towards the best alternative:
            every {
                simuler(
                    simuleringSpec(
                        foersteUttakDato = LocalDate.of(2032, 2, 1),
                        uttaksgrad = UttakGradKode.P_40,
                        heltUttakDato = LocalDate.of(2033, 2, 1)
                    )
                )
            } throws UtilstrekkeligOpptjeningException()

            every {
                simuler(
                    simuleringSpec(
                        foersteUttakDato = LocalDate.of(2033, 1, 1),
                        uttaksgrad = UttakGradKode.P_40,
                        heltUttakDato = LocalDate.of(2033, 8, 1)
                    )
                )
            } returns SimulatorOutput()

            every {
                simuler(
                    simuleringSpec(
                        foersteUttakDato = LocalDate.of(2032, 7, 1),
                        uttaksgrad = UttakGradKode.P_40,
                        heltUttakDato = LocalDate.of(2033, 5, 1)
                    )
                )
            } returns SimulatorOutput()

            every {
                simuler(
                    simuleringSpec(
                        foersteUttakDato = LocalDate.of(2032, 4, 1),
                        uttaksgrad = UttakGradKode.P_40,
                        heltUttakDato = LocalDate.of(2033, 3, 1)
                    )
                )
            } returns SimulatorOutput()

            every {
                simuler(
                    simuleringSpec(
                        foersteUttakDato = LocalDate.of(2032, 3, 1),
                        uttaksgrad = UttakGradKode.P_40,
                        heltUttakDato = LocalDate.of(2033, 3, 1)
                    )
                )
            } returns SimulatorOutput()
        }

    /**
     * IndexBasedSimulering.tryIndex: discriminator.simuler(indexSimulatorSpec)
     */
    fun simuleringSpec(foersteUttakDato: LocalDate, uttaksgrad: UttakGradKode, heltUttakDato: LocalDate) =
        SimuleringSpec(
            type = SimuleringTypeEnum.ALDER_M_AFP_PRIVAT,
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
}
