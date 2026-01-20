package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.Pre2025OffentligAfpSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.inntekt.InntektService
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.trygdetid.UtlandPeriode
import java.time.LocalDate

class SimulerOffentligTjenestepensjonSpecMapperV3Test : ShouldSpec({

    should("map all fields including nested lists and optionals") {
        val mapper = SimulerOffentligTjenestepensjonSpecMapperV3(
            personService = arrangePerson(),
            inntektService = arrangeInntekt()
        )

        val specDto = SimulerOffentligTjenestepensjonSpecV3(
            simuleringEtter2011 = SimuleringEtter2011SpecV3(
                simuleringType = SimuleringTypeSpecV3.AFP_ETTERF_ALDER,
                fnr = Fnr("12345678901"),
                fnrAvdod = Fnr("10987654321"),
                fodselsar = 1960,
                ansettelsessektor = AnsattTypeCodeV3.OFFENTLIG,
                offentligAfpRett = true,
                privatAfpRett = false,
                simuleringsvalgOffentligAfp = null,
                samtykke = true,
                forventetInntekt = 1,
                antArInntektOverG = 2,
                forsteUttakDato = LocalDate.of(2027, 2, 1),
                utg = UttaksgradSpecV3.P_100,
                inntektUnderGradertUttak = 3,
                heltUttakDato = null,
                inntektEtterHeltUttak = 4,
                antallArInntektEtterHeltUttak = 5,
                utenlandsopphold = 6,
                flyktning = true,
                sivilstatus = SivilstatusSpecV3.GIFT,
                epsPensjon = true,
                eps2G = true,
                afpOrdning = AfpOrdningTypeSpecV3.AFPSTAT,
                afpInntektMndForUttak = true,
                brukerRegTPListe = emptyList(),
                stillingsprosentOffHeltUttak = StillingsprOffCodeV3.P_0,
                stillingsprosentOffGradertUttak = StillingsprOffCodeV3.P_100,
                dodsdato = LocalDate.of(2025, 2, 3),
                avdodAntallArIUtlandet = 6,
                avdodInntektForDod = 7,
                inntektAvdodOver1G = true,
                avdodMedlemAvFolketrygden = true,
                avdodFlyktning = true,
                simulerForTp = null,
                utenlandsperiodeForSimuleringList = listOf(
                    UtenlandsperiodeForSimuleringV3(
                        periodeFom = LocalDate.of(2024, 1, 15),
                        periodeTom = LocalDate.of(2025, 1, 15),
                        land = "SWE",
                        arbeidetIUtland = true
                    ),
                    UtenlandsperiodeForSimuleringV3(
                        periodeFom = LocalDate.of(2021, 1, 15),
                        periodeTom = LocalDate.of(2024, 1, 15),
                        land = "CAN",
                        arbeidetIUtland = true
                    )
                ),
                fremtidigInntektList = emptyList()
            )
        )

        mapper.fromDto(dto = specDto) shouldBe
                SimuleringSpec(
                    type = SimuleringTypeEnum.AFP_ETTERF_ALDER,
                    sivilstatus = SivilstatusType.GIFT,
                    epsHarPensjon = true,
                    foersteUttakDato = LocalDate.of(2027, 2, 1),
                    heltUttakDato = null,
                    pid = Pid("12345678901"),
                    foedselDato = LocalDate.of(1960, 6, 10),
                    avdoed = Avdoed(
                        pid = Pid("10987654321"),
                        antallAarUtenlands = 6,
                        inntektFoerDoed = 7,
                        doedDato = LocalDate.of(2025, 2, 3),
                        erMedlemAvFolketrygden = true,
                        harInntektOver1G = true
                    ),
                    isTpOrigSimulering = false,
                    simulerForTp = false,
                    uttakGrad = UttakGradKode.P_100,
                    forventetInntektBeloep = 1,
                    inntektUnderGradertUttakBeloep = 3,
                    inntektEtterHeltUttakBeloep = 4,
                    inntektEtterHeltUttakAntallAar = 5,
                    foedselAar = 1960,
                    utlandAntallAar = 6,
                    utlandPeriodeListe = mutableListOf(
                        UtlandPeriode(
                            fom = LocalDate.of(2024, 1, 15),
                            tom = LocalDate.of(2025, 1, 15),
                            land = LandkodeEnum.SWE,
                            arbeidet = true
                        ),
                        UtlandPeriode(
                            fom = LocalDate.of(2021, 1, 15),
                            tom = LocalDate.of(2024, 1, 15),
                            land = LandkodeEnum.CAN,
                            arbeidet = true
                        )
                    ),
                    fremtidigInntektListe = mutableListOf(),
                    brukFremtidigInntekt = false,
                    inntektOver1GAntallAar = 0,
                    flyktning = true,
                    epsHarInntektOver2G = true,
                    livsvarigOffentligAfp = null,
                    pre2025OffentligAfp = Pre2025OffentligAfpSpec(
                        afpOrdning = AFPtypeEnum.AFPSTAT,
                        inntektMaanedenFoerAfpUttakBeloep = 123,
                        inntektUnderAfpUttakBeloep = 3
                    ),
                    erAnonym = false,
                    ignoreAvslag = false,
                    isHentPensjonsbeholdninger = true,
                    isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true,
                    onlyVilkaarsproeving = false,
                    epsKanOverskrives = false,
                    registerData = null
                )
    }
})

private fun arrangeInntekt(): InntektService =
    mockk<InntektService>().apply {
        every {
            hentSisteMaanedsInntektOver1G(any())
        } returns 123
    }

private fun arrangePerson(): GeneralPersonService =
    mockk<GeneralPersonService>().apply {
        every {
            foedselsdato(any())
        } returns LocalDate.of(1960, 6, 10)
    }
