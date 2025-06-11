package no.nav.pensjon.simulator.alderspensjon.alternativ

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.alderspensjon.alternativ.UfoereAlternativSimuleringServiceTestObjects.arrangeAvslaattUtkanttilfelle
import no.nav.pensjon.simulator.alderspensjon.alternativ.UfoereAlternativSimuleringServiceTestObjects.arrangeGradertUttakEtterAvslaatt60ProsentUttak
import no.nav.pensjon.simulator.alderspensjon.alternativ.UfoereAlternativSimuleringServiceTestObjects.arrangeGradertUttakEtterAvslaattHeltUttak
import no.nav.pensjon.simulator.alderspensjon.alternativ.UfoereAlternativSimuleringServiceTestObjects.arrangeIngenInnvilgedeUttak
import no.nav.pensjon.simulator.alderspensjon.alternativ.UfoereAlternativSimuleringServiceTestObjects.foedselsdato
import no.nav.pensjon.simulator.alderspensjon.alternativ.UfoereAlternativSimuleringServiceTestObjects.normertPensjoneringsdato
import no.nav.pensjon.simulator.alderspensjon.alternativ.UfoereAlternativSimuleringServiceTestObjects.simuleringSpec
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligOpptjeningException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligTrygdetidException
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class UfoereAlternativSimuleringServiceTest : FunSpec({

    test("simulerAlternativHvisUtkanttilfelletInnvilges should return no result if utkanttilfellet avslått") {
        val service = UfoereAlternativSimuleringService(
            simulator = arrangeAvslaattUtkanttilfelle(),
            normalderService = Arrange.normalder(foedselsdato()),
            alternativtUttakService = mockk(),
            time = { LocalDate.of(2025, 1, 1) }
        )

        service.simulerAlternativHvisUtkanttilfelletInnvilges(
            spec = simuleringSpec(
                foersteUttakDato = LocalDate.of(2030, 2, 1),
                uttaksgrad = UttakGradKode.P_60,
                heltUttakDato = LocalDate.of(2032, 2, 1)
            )
        )?.alternativ shouldBe SimulertAlternativ(
            gradertUttakAlder = null,
            uttakGrad = UttakGradKode.P_0,
            heltUttakAlder = SimulertUttakAlder(alder = Alder(0, 0), uttakDato = LocalDate.MIN),
            resultStatus = SimulatorResultStatus.NONE // i.e. no result
        )
    }

    test("simulerMedFallendeUttaksgrad for gradert uttak should return: alternativ med lavere grad men samme uttaksdato") {
        val service = UfoereAlternativSimuleringService(
            simulator = arrangeGradertUttakEtterAvslaatt60ProsentUttak(),
            normalderService = Arrange.normalder(foedselsdato()),
            alternativtUttakService = mockk(),
            time = { LocalDate.of(2025, 1, 1) }
        )

        service.simulerMedFallendeUttaksgrad(
            spec = simuleringSpec(
                foersteUttakDato = LocalDate.of(2030, 2, 1),
                uttaksgrad = UttakGradKode.P_60,
                heltUttakDato = LocalDate.of(2032, 2, 1)
            ),
            exception = UtilstrekkeligOpptjeningException()
        ).alternativ shouldBe SimulertAlternativ(
            gradertUttakAlder = SimulertUttakAlder(alder = Alder(63, 0), uttakDato = LocalDate.of(2030, 2, 1)),
            uttakGrad = UttakGradKode.P_40,
            heltUttakAlder = SimulertUttakAlder(alder = Alder(65, 0), uttakDato = LocalDate.of(2032, 2, 1)),
            resultStatus = SimulatorResultStatus.GOOD
        )
    }

    test("simulerMedFallendeUttaksgrad for helt uttak should return: alternativ med lavere grad med helt uttak-dato som ny gradert uttak-dato") {
        val service = UfoereAlternativSimuleringService(
            simulator = arrangeGradertUttakEtterAvslaattHeltUttak(),
            normalderService = Arrange.normalder(foedselsdato()),
            alternativtUttakService = mockk(),
            time = { LocalDate.of(2025, 1, 1) }
        )

        service.simulerMedFallendeUttaksgrad(
            spec = simuleringSpec(
                foersteUttakDato = LocalDate.of(2030, 2, 1),
                uttaksgrad = UttakGradKode.P_100,
                heltUttakDato = null // brukes bare ved gradert uttak (etterfulgt av helt uttak)
            ),
            exception = UtilstrekkeligTrygdetidException()
        ).alternativ shouldBe SimulertAlternativ(
            gradertUttakAlder = SimulertUttakAlder(alder = Alder(63, 0), uttakDato = LocalDate.of(2030, 2, 1)),
            uttakGrad = UttakGradKode.P_60,
            heltUttakAlder = SimulertUttakAlder(alder = Alder(67, 0), uttakDato = normertPensjoneringsdato()),
            resultStatus = SimulatorResultStatus.GOOD
        )
    }

    test("simulerMedFallendeUttaksgrad for ingen innvilgede uttak should throw exception") {
        val service = UfoereAlternativSimuleringService(
            simulator = arrangeIngenInnvilgedeUttak(),
            normalderService = Arrange.normalder(foedselsdato()),
            alternativtUttakService = mockk(),
            time = { LocalDate.of(2025, 1, 1) }
        )

        shouldThrow<UtilstrekkeligOpptjeningException> {
            service.simulerMedFallendeUttaksgrad(
                spec = simuleringSpec(
                    foersteUttakDato = LocalDate.of(2030, 2, 1),
                    uttaksgrad = UttakGradKode.P_50,
                    heltUttakDato = LocalDate.of(2032, 2, 1)
                ),
                exception = UtilstrekkeligOpptjeningException()
            )
        }.message shouldBe null
    }
})

private object UfoereAlternativSimuleringServiceTestObjects {

    fun arrangeGradertUttakEtterAvslaatt60ProsentUttak(): SimulatorCore =
        mockk<SimulatorCore>().apply {
            arrangeFoedselsdato(this)

            // Etter avslag på 60 % forsøkes det med neste lavere uttaksgrad (50 %):
            every {
                simuler(
                    simuleringSpec(
                        foersteUttakDato = LocalDate.of(2030, 2, 1),
                        uttaksgrad = UttakGradKode.P_50,
                        heltUttakDato = LocalDate.of(2032, 2, 1)
                    )
                )
            } throws UtilstrekkeligOpptjeningException() // => avslag igjen

            // Etter avslag på 50 % forsøkes det med neste lavere uttaksgrad (40 %):
            every {
                simuler(
                    simuleringSpec(
                        foersteUttakDato = LocalDate.of(2030, 2, 1),
                        uttaksgrad = UttakGradKode.P_40,
                        heltUttakDato = LocalDate.of(2032, 2, 1)
                    )
                )
            } returns SimulatorOutput() // => innvilget
        }

    fun arrangeGradertUttakEtterAvslaattHeltUttak(): SimulatorCore =
        mockk<SimulatorCore>().apply {
            arrangeFoedselsdato(this)

            // Etter avslått helt uttak forsøkes det med høyeste graderte uttaksgrad (80 %):
            every {
                simuler(
                    simuleringSpec(
                        foersteUttakDato = LocalDate.of(2030, 2, 1),
                        uttaksgrad = UttakGradKode.P_80,
                        heltUttakDato = normertPensjoneringsdato()
                    )
                )
            } throws UtilstrekkeligOpptjeningException() // => avslag igjen

            // Etter avslag på 80 % forsøkes det med neste lavere uttaksgrad (60 %):
            every {
                simuler(
                    simuleringSpec(
                        foersteUttakDato = LocalDate.of(2030, 2, 1),
                        uttaksgrad = UttakGradKode.P_60,
                        heltUttakDato = normertPensjoneringsdato()
                    )
                )
            } returns SimulatorOutput() // => innvilget
        }

    fun arrangeIngenInnvilgedeUttak(): SimulatorCore =
        mockk<SimulatorCore>().apply {
            arrangeFoedselsdato(this)

            // Etter avslag på 50 % forsøkes det med neste lavere uttaksgrad (40 %):
            every {
                simuler(
                    simuleringSpec(
                        foersteUttakDato = LocalDate.of(2030, 2, 1),
                        uttaksgrad = UttakGradKode.P_40,
                        heltUttakDato = LocalDate.of(2032, 2, 1)
                    )
                )
            } throws UtilstrekkeligTrygdetidException() // => avslag

            // Siste forsøk: Laveste uttaksgrad (20 %):
            every {
                simuler(
                    simuleringSpec(
                        foersteUttakDato = LocalDate.of(2030, 2, 1),
                        uttaksgrad = UttakGradKode.P_20,
                        heltUttakDato = LocalDate.of(2032, 2, 1)
                    )
                )
            } throws UtilstrekkeligOpptjeningException() // => avslag igjen
        }

    fun arrangeAvslaattUtkanttilfelle(): SimulatorCore =
        mockk<SimulatorCore>().apply {
            arrangeFoedselsdato(this)

            every {
                simuler(
                    simuleringSpec(
                        foersteUttakDato = LocalDate.of(2030, 2, 1), // konstant dato for gradert uttak
                        uttaksgrad = UttakGradKode.P_20, // minste uttaksgrad
                        heltUttakDato = normertPensjoneringsdato()
                    )
                )
            } throws UtilstrekkeligTrygdetidException()
        }

    fun foedselsdato(): LocalDate = LocalDate.of(1967, 1, 9)

    /**
     * Normert pensjoneringsdato = fødselsmånedens 1. dag + normalder + 1 måned
     * I dette tilfellet blir datoen: 1967-01-01 + 67 år + 1 måned = 2034-02-01
     */
    fun normertPensjoneringsdato() = LocalDate.of(2034, 2, 1)

    /**
     * IndexBasedSimulering.tryIndex: discriminator.simuler(indexSimulatorSpec)
     */
    fun simuleringSpec(foersteUttakDato: LocalDate, uttaksgrad: UttakGradKode, heltUttakDato: LocalDate?) =
        SimuleringSpec(
            type = SimuleringTypeEnum.ALDER_M_AFP_PRIVAT,
            sivilstatus = SivilstatusType.UGIF,
            epsHarPensjon = false,
            foersteUttakDato = foersteUttakDato,
            heltUttakDato = heltUttakDato,
            pid = pid,
            foedselDato = foedselsdato(),
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

    private fun arrangeFoedselsdato(simulator: SimulatorCore) {
        every { simulator.fetchFoedselsdato(pid) } returns foedselsdato()
    }
}
