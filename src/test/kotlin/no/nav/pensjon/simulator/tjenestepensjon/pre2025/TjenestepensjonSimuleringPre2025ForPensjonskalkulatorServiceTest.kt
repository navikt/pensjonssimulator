package no.nav.pensjon.simulator.tjenestepensjon.pre2025

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.inntekt.InntektService
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3.*
import java.time.LocalDate

class TjenestepensjonSimuleringPre2025ForPensjonskalkulatorServiceTest : StringSpec({

    "alle mulige felter blir brukt i mapping til simuleringSpec" {
        val mapper = SimulerOffentligTjenestepensjonSpecMapperV3(
            personService = arrangePerson(),
            inntektService = arrangeInntekt()
        )

        val spec = mockSpec(simuleringType = SimuleringTypeSpecV3.AFP_ETTERF_ALDER)

        with(mapper.fromDto(dto = spec)) {
            type shouldBe SimuleringTypeEnum.AFP_ETTERF_ALDER
            sivilstatus shouldBe SivilstatusType.GIFT
            epsHarPensjon shouldBe true
            epsHarInntektOver2G shouldBe true
            foersteUttakDato shouldBe LocalDate.of(2027, 2, 1)
            heltUttakDato shouldBe null
            pid?.value shouldBe "12345678901"
            foedselDato shouldBe LocalDate.of(1960, 1, 1)
            avdoed shouldNotBe null
            with(avdoed!!) {
                pid.value shouldBe "10987654321"
                antallAarUtenlands shouldBe 6
                inntektFoerDoed shouldBe 7
                harInntektOver1G shouldBe true
                erMedlemAvFolketrygden shouldBe true
                doedDato shouldBe LocalDate.of(2025, 2, 3)
            }
            uttakGrad shouldBe UttakGradKode.P_100
            forventetInntektBeloep shouldBe 1
            inntektUnderGradertUttakBeloep shouldBe 3
            inntektEtterHeltUttakBeloep shouldBe 4
            inntektEtterHeltUttakAntallAar shouldBe 5
            foedselAar shouldBe 1960
            utlandAntallAar shouldBe 6
            utlandPeriodeListe.size shouldBe 2
            with(utlandPeriodeListe[0]) {
                land shouldBe LandkodeEnum.SWE
                fom shouldBe idag.minusYears(2)
                tom shouldBe idag.minusYears(1)
                arbeidet shouldBe true
            }
            with(utlandPeriodeListe[1]) {
                land shouldBe LandkodeEnum.CAN
                fom shouldBe idag.minusYears(5)
                tom shouldBe idag.minusYears(2)
                arbeidet shouldBe true
            }
            flyktning shouldBe true
            pre2025OffentligAfp shouldNotBe null
            with(pre2025OffentligAfp!!) {
                afpOrdning shouldBe AFPtypeEnum.AFPSTAT
                inntektMaanedenFoerAfpUttakBeloep shouldBe 6
                inntektUnderAfpUttakBeloep shouldBe 3
            }

            // Default-verdier uansett hva som er satt i DTO:
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter shouldBe true
            isHentPensjonsbeholdninger shouldBe true
            inntektOver1GAntallAar shouldBe 0
            brukFremtidigInntekt shouldBe false
            fremtidigInntektListe shouldBe emptyList()
            simulerForTp shouldBe false
            isTpOrigSimulering shouldBe false
            erAnonym shouldBe false
            ignoreAvslag shouldBe false
            onlyVilkaarsproeving shouldBe false
            epsKanOverskrives shouldBe false
        }
    }

    "offentlig AFP blir ikke mappet ved andre simuleringstyper enn AFP_ETTERF_ALDER" {
        val mapper = SimulerOffentligTjenestepensjonSpecMapperV3(
            personService = arrangePerson(),
            inntektService = mockk()
        )

        mapper.fromDto(
            dto = mockSpec(simuleringType = SimuleringTypeSpecV3.ALDER)
        ).pre2025OffentligAfp shouldBe null
    }

    "mapping tåler default-verdier fra spec" {
        val mapper = SimulerOffentligTjenestepensjonSpecMapperV3(
            personService = arrangePerson(),
            inntektService = mockk()
        )

        with(mapper.fromDto(mockMinimalSpec())) {
            type shouldBe SimuleringTypeEnum.ALDER_M_AFP_PRIVAT
            sivilstatus shouldBe SivilstatusType.UGIF
            epsHarPensjon shouldBe false
            epsHarInntektOver2G shouldBe false
            foersteUttakDato shouldBe LocalDate.of(2027, 2, 1)
            heltUttakDato shouldBe null
            pid?.value shouldBe "12345678901"
            foedselDato shouldBe LocalDate.of(1960, 1, 1)
            avdoed shouldBe null
            uttakGrad shouldBe UttakGradKode.P_100
            forventetInntektBeloep shouldBe 0
            inntektUnderGradertUttakBeloep shouldBe 0
            inntektEtterHeltUttakBeloep shouldBe 0
            inntektEtterHeltUttakAntallAar shouldBe 0
            foedselAar shouldBe 0
            utlandAntallAar shouldBe 0
            utlandPeriodeListe.size shouldBe 0
            flyktning shouldBe null
            pre2025OffentligAfp shouldBe null

            // Default-verdier uansett hva som er satt i DTO:
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter shouldBe true
            isHentPensjonsbeholdninger shouldBe true
            inntektOver1GAntallAar shouldBe 0
            brukFremtidigInntekt shouldBe false
            fremtidigInntektListe shouldBe emptyList()
            simulerForTp shouldBe false
            isTpOrigSimulering shouldBe false
            erAnonym shouldBe false
            ignoreAvslag shouldBe false
            onlyVilkaarsproeving shouldBe false
            epsKanOverskrives shouldBe false
        }
    }
})

private val idag = LocalDate.now()

private fun arrangeInntekt(): InntektService =
    mockk<InntektService>().apply {
        every {
            hentSisteMaanedsInntektOver1G(any())
        } returns 6
    }

private fun arrangePerson(): GeneralPersonService =
    mockk<GeneralPersonService>().apply {
        every {
            foedselsdato(any())
        } returns LocalDate.of(1960, 1, 1)
    }

private fun mockMinimalSpec() =
    SimulerOffentligTjenestepensjonSpecV3(
        simuleringEtter2011 = SimuleringEtter2011SpecV3(
            simuleringType = SimuleringTypeSpecV3.ALDER_M_AFP_PRIVAT,
            fnr = Fnr("12345678901"),
            forsteUttakDato = LocalDate.of(2027, 2, 1)
        )
    )

private fun mockSpec(simuleringType: SimuleringTypeSpecV3) =
    SimulerOffentligTjenestepensjonSpecV3(
        simuleringEtter2011 = SimuleringEtter2011SpecV3(
            simuleringType, // hvis ALDER vil ikke afpOrdning bli satt
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
                    periodeFom = idag.minusYears(2),
                    periodeTom = idag.minusYears(1),
                    land = "SWE",
                    arbeidetIUtland = true
                ),
                UtenlandsperiodeForSimuleringV3(
                    periodeFom = idag.minusYears(5),
                    periodeTom = idag.minusYears(2),
                    land = "CAN",
                    arbeidetIUtland = true
                )
            ),
            fremtidigInntektList = emptyList()
        )
    )