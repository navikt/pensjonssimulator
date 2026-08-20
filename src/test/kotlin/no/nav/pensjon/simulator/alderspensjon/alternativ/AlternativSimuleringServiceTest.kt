package no.nav.pensjon.simulator.alderspensjon.alternativ


import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligTrygdetidException
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class AlternativSimuleringServiceTest : ShouldSpec({

    context("simulerAlternativHvisUtkanttilfelletInnvilges") {
        context("utkanttilfellet avslått") {
            should("gi resultat for ubetinget uttak (100 % ved normalder)") {
                val service = AlternativSimuleringService(
                    simulator = arrangeAvslaattUtkanttilfelle(),
                    normalderService = Arrange.normalder(foedselsdato), // 67 år 0 måneder
                    alternativtUttakService = mockk(),
                    time = { LocalDate.of(2025, 1, 1) }
                )

                service.simulerAlternativHvisUtkanttilfelletInnvilges(
                    spec = simuleringSpec(
                        foersteUttakDato = LocalDate.of(2030, 2, 1),
                        uttaksgrad = UttakGradKode.P_60,
                        heltUttakDato = LocalDate.of(2032, 2, 1)
                    ),
                    inkluderPensjonHvisUbetinget = false
                )?.alternativ shouldBe SimulertAlternativ(
                    gradertUttakAlder = null,
                    uttakGrad = UttakGradKode.P_100,
                    heltUttakAlder = SimulertUttakAlder(
                        alder = Alder(aar = 67, maaneder = 0),
                        uttakDato = LocalDate.of(2034, 2, 1)
                    ),
                    resultStatus = SimulatorResultStatus.GOOD
                )
            }
        }
    }
})

private val foedselsdato = LocalDate.of(1967, 1, 9)

/**
 * Normert pensjoneringsdato = fødselsmånedens 1. dag + normalder + 1 måned
 * I dette tilfellet blir datoen: 1967-01-01 + 67 år + 1 måned = 2034-02-01
 */
private val normertPensjoneringsdato = LocalDate.of(2034, 2, 1)

private fun arrangeFoedselsdato(simulator: SimulatorCore) {
    every { simulator.fetchFoedselsdato(pid) } returns foedselsdato
}

private fun arrangeAvslaattUtkanttilfelle(): SimulatorCore =
    mockk {
        arrangeFoedselsdato(this)

        every {
            simuler(
                simuleringSpec(
                    foersteUttakDato = LocalDate.of(2034, 1, 1),
                    uttaksgrad = UttakGradKode.P_20, // minste uttaksgrad
                    heltUttakDato = normertPensjoneringsdato
                )
            )
        } throws UtilstrekkeligTrygdetidException()
    }

private fun simuleringSpec(
    foersteUttakDato: LocalDate,
    uttaksgrad: UttakGradKode,
    heltUttakDato: LocalDate?
) =
    SimuleringSpec(
        type = SimuleringTypeEnum.ALDER_M_AFP_PRIVAT,
        sivilstatus = SivilstatusType.UGIF,
        epsHarPensjon = false,
        foersteUttakDato = foersteUttakDato,
        heltUttakDato = heltUttakDato,
        pid = pid,
        foedselDato = foedselsdato,
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
        livsvarigOffentligAfp = null,
        tidsbegrensetOffentligAfp = null,
        erAnonym = false,
        ignoreAvslag = false,
        isHentPensjonsbeholdninger = true,
        isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
        onlyVilkaarsproeving = false,
        epsKanOverskrives = false,
        tillatSenereFoersteuttakForUfoere = false
    )