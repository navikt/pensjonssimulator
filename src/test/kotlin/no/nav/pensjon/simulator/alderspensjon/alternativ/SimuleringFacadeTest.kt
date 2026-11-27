package no.nav.pensjon.simulator.alderspensjon.alternativ

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpOutput
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.SimulatorCore
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligOpptjeningException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligTrygdetidException
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.core.ufoere.UfoereService
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import no.nav.pensjon.simulator.validity.Problem
import no.nav.pensjon.simulator.validity.ProblemType
import java.time.LocalDate

class SimuleringFacadeTest : ShouldSpec({

    context("utilstrekkelig opptjening ved alternativ simulering") {
        should("returnere resultat med problembeskrivelse") {
            SimuleringFacade(
                simulator = arrangeUtilstrekkeligOpptjening(),
                alternativSimulering = arrangeUtilstrekkeligOpptjeningAlternativ(),
                ufoereAlternativSimulering = mockk(),
                normalderService = arrangeNormalder(),
                ufoereService = mockk(relaxed = true),
                outputConverter = mockk(),
                time = mockk(relaxed = true),
            ).simulerAlderspensjon(
                spec = simuleringSpec(
                    foedselsdato = LocalDate.of(1965, 6, 15),
                    foersteUttakDato = LocalDate.of(2030, 7, 1)
                ),
                inkluderPensjonHvisUbetinget = false
            ) shouldBe
                    SimulertPensjonEllerAlternativ(
                        pensjon = null,
                        alternativ = null,
                        problem = Problem(
                            type = ProblemType.UTILSTREKKELIG_OPPTJENING,
                            beskrivelse = "10 kr"
                        )
                    )
        }
    }

    context("utilstrekkelig trygdetid etter normert pensjonsalder") {
        should("returnere resultat med problembeskrivelse") {
            val alternativSimulering: AlternativSimuleringService = mockk()

            SimuleringFacade(
                simulator = arrangeUtilstrekkeligTrygdetid(),
                alternativSimulering,
                ufoereAlternativSimulering = mockk(),
                normalderService = arrangeNormalder(),
                ufoereService = mockk(relaxed = true),
                outputConverter = mockk(),
                time = mockk(relaxed = true),
            ).simulerAlderspensjon(
                spec = simuleringSpec(
                    foedselsdato = LocalDate.of(1960, 6, 15),
                    foersteUttakDato = LocalDate.of(2030, 7, 1) // 70 år ved uttak
                ),
                inkluderPensjonHvisUbetinget = false
            ) shouldBe
                    SimulertPensjonEllerAlternativ(
                        pensjon = null,
                        alternativ = null,
                        problem = Problem(
                            type = ProblemType.UTILSTREKKELIG_TRYGDETID,
                            beskrivelse = "1 år"
                        )
                    )

            verify { alternativSimulering wasNot Called }
        }
    }

    context("utilstrekkelig opptjening på normert pensjonsalder") {
        should("returnere resultat med problembeskrivelse") {
            val alternativSimulering: AlternativSimuleringService = mockk()

            SimuleringFacade(
                simulator = arrangeUtilstrekkeligOpptjening(),
                alternativSimulering,
                ufoereAlternativSimulering = mockk(),
                normalderService = arrangeNormalder(Alder(aar = 67, maaneder = 4)),
                ufoereService = mockk(relaxed = true),
                outputConverter = mockk(),
                time = mockk(relaxed = true),
            ).simulerAlderspensjon(
                spec = simuleringSpec(
                    foedselsdato = LocalDate.of(1970, 2, 15),
                    foersteUttakDato = LocalDate.of(2037, 7, 1), // 67 år 4 md ved uttak
                    heltUttakDato = null
                ),
                inkluderPensjonHvisUbetinget = false
            ) shouldBe
                    SimulertPensjonEllerAlternativ(
                        pensjon = null,
                        alternativ = null,
                        problem = Problem(
                            type = ProblemType.UTILSTREKKELIG_OPPTJENING,
                            beskrivelse = "10 kr"
                        )
                    )

            verify { alternativSimulering wasNot Called }
        }
    }

    context("ufør med AFP, senere førsteuttak tillatt") {
        should("bruke normal NAU-logikk, ikke uførespesifikk") {
            val normalNauLogikk: AlternativSimuleringService = mockk(relaxed = true)
            val ufoereNauLogikk: UfoereAlternativSimuleringService = mockk()

            SimuleringFacade(
                simulator = arrangeUtilstrekkeligOpptjening(), // medfører NAU (Nært Angitt Uttak)
                alternativSimulering = normalNauLogikk,
                ufoereAlternativSimulering = ufoereNauLogikk,
                normalderService = arrangeNormalder(),
                ufoereService = arrangeUfoer(), // ufør
                outputConverter = mockk(),
                time = mockk(relaxed = true),
            ).simulerAlderspensjon(
                simuleringSpec(
                    foedselsdato = LocalDate.of(1965, 6, 15),
                    foersteUttakDato = LocalDate.of(2030, 7, 1),
                    type = SimuleringTypeEnum.ALDER_M_AFP_PRIVAT, // med AFP
                    tillatSenereFoersteuttakForUfoere = true // senere førsteuttak tillatt
                ),
                inkluderPensjonHvisUbetinget = false
            )

            verify(exactly = 1) { normalNauLogikk.simulerMedNesteLavereUttaksgrad(any(), any()) }
            verify { ufoereNauLogikk wasNot Called }
        }
    }

    context("livsvarig AFP i offentlig sektor") {
        should("sjekke om AFP påvirker vilkårsprøvingen") {
            val simulator: SimulatorCore = mockk {
                every { simuler(any()) } returns SimulatorOutput(
                    livsvarigOffentligAfp = listOf(
                        LivsvarigOffentligAfpOutput(
                            alderAar = 65,
                            beloep = 12000,
                            maanedligBeloep = 1000
                        )
                    )
                )
            }

            SimuleringFacade(
                simulator,
                alternativSimulering = mockk(),
                ufoereAlternativSimulering = mockk(),
                normalderService = mockk(relaxed = true),
                ufoereService = mockk(relaxed = true),
                outputConverter = mockk(relaxed = true),
                time = mockk(relaxed = true),
            ).simulerAlderspensjon(
                simuleringSpec(
                    type = SimuleringTypeEnum.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG,
                    livsvarigOffentligAfp = LivsvarigOffentligAfpSpec(
                        vedInnvilgelseVilkaarsproevOgsaaUtenAfp = true
                    )
                ),
                inkluderPensjonHvisUbetinget = false
            )

            verify(exactly = 1) { simulator.simuler(match { it.type == SimuleringTypeEnum.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG }) }
            // Bekreft at sjekken ble utført:
            verify(exactly = 1) { simulator.simuler(match { it.type == SimuleringTypeEnum.ALDER && it.onlyVilkaarsproeving }) }
        }
    }
})

private fun arrangeNormalder(alder: Alder = Alder(aar = 67, maaneder = 0)): NormertPensjonsalderService =
    mockk { every { normalder(any<LocalDate>()) } returns alder }

private fun arrangeUfoer(): UfoereService =
    mockk { every { hasUfoereperiode(any(), any()) } returns true }

private fun arrangeUtilstrekkeligOpptjening(): SimulatorCore =
    mockk { every { simuler(any()) } throws UtilstrekkeligOpptjeningException("10 kr") }

private fun arrangeUtilstrekkeligTrygdetid(): SimulatorCore =
    mockk { every { simuler(any()) } throws UtilstrekkeligTrygdetidException("1 år") }

private fun arrangeUtilstrekkeligOpptjeningAlternativ(): AlternativSimuleringService =
    mockk {
        every {
            simulerMedNesteLavereUttaksgrad(any(), any())
        } throws UtilstrekkeligOpptjeningException("10 kr")
    }