package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.inntekt.InntektService
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2.*
import java.time.LocalDate

class SimulerOffentligTjenestepensjonMapperV2Test : StringSpec({

    "alle mulige felter blir brukt i mapping mot til simuleringSpec" {
        val personService = mockk<GeneralPersonService>().apply {
            every { foedselsdato(any()) } returns LocalDate.of(1960, 1, 1)
        }
        val inntektService = mockk<InntektService>().apply {
            every { hentSisteMaanedsInntektOver1G(true) } returns 6
        }
        val mapper = SimulerOffentligTjenestepensjonMapperV2(personService, inntektService)

        val spec = mockSpec(simuleringType = SimuleringTypeSpecV2.AFP_ETTERF_ALDER)

        with(mapper.fromDto(spec)) {
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
                this.pid.value shouldBe "10987654321"
                this.antallAarUtenlands shouldBe 6
                this.inntektFoerDoed shouldBe 7
                this.harInntektOver1G shouldBe true
                this.erMedlemAvFolketrygden shouldBe true
                this.doedDato shouldBe LocalDate.of(2025, 2, 3)
            }
            uttakGrad shouldBe UttakGradKode.P_100
            forventetInntektBeloep shouldBe 1
            inntektUnderGradertUttakBeloep shouldBe 3
            inntektEtterHeltUttakBeloep shouldBe 4
            inntektEtterHeltUttakAntallAar shouldBe 5
            foedselAar shouldBe 1960
            utlandAntallAar shouldBe 6
            utlandPeriodeListe.size shouldBe 2
            with (utlandPeriodeListe[0]){
                land shouldBe LandkodeEnum.SWE
                fom shouldBe LocalDate.now().minusYears(2)
                tom shouldBe LocalDate.now().minusYears(1)
                arbeidet shouldBe true
            }
            with(utlandPeriodeListe[1]) {
                land shouldBe LandkodeEnum.CAN
                fom shouldBe LocalDate.now().minusYears(5)
                tom shouldBe LocalDate.now().minusYears(2)
                arbeidet shouldBe true
            }
            flyktning shouldBe true
            pre2025OffentligAfp shouldNotBe null
            with (pre2025OffentligAfp!!) {
                this.afpOrdning shouldBe AFPtypeEnum.AFPSTAT
                this.inntektMaanedenFoerAfpUttakBeloep shouldBe 6
                this.inntektUnderAfpUttakBeloep shouldBe 3
            }

            //default verdier uansett hva som er satt i dto
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

    "offentlig AFP blir ikke mappet ved andre simuleringstype enn AFP_ETTERF_ALDER" {
        val personService = mockk<GeneralPersonService>().apply {
            every { foedselsdato(any()) } returns LocalDate.of(1960, 1, 1)
        }
        val inntektService = mockk<InntektService>()
        val mapper = SimulerOffentligTjenestepensjonMapperV2(personService, inntektService)

        val simSpec = mapper.fromDto(mockSpec(simuleringType = SimuleringTypeSpecV2.ALDER))

        simSpec.pre2025OffentligAfp shouldBe null
    }

    "mapping tåler default verdier fra spec" {
        val personService = mockk<GeneralPersonService>().apply {
            every { foedselsdato(any()) } returns LocalDate.of(1960, 1, 1)
        }
        val inntektService = mockk<InntektService>()
        val mapper = SimulerOffentligTjenestepensjonMapperV2(personService, inntektService)

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

            //default verdier uansett hva som er satt i dto
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

    "land konverteres til enum" {
        val mapper = SimulerOffentligTjenestepensjonMapperV2(mockk(), mockk())

        mapper.mapLand("NOR") shouldBe LandkodeEnum.NOR
        mapper.mapLand("SWE") shouldBe LandkodeEnum.SWE
        mapper.mapLand("USA") shouldBe LandkodeEnum.USA
        mapper.mapLand("CAN") shouldBe LandkodeEnum.CAN
        mapper.mapLand("FIN") shouldBe LandkodeEnum.FIN
        mapper.mapLand("DNK") shouldBe LandkodeEnum.DNK
        mapper.mapLand("???") shouldBe LandkodeEnum.P_UKJENT
        mapper.mapLand("349") shouldBe LandkodeEnum.P_SPANSKE_OMR_AFRIKA
        mapper.mapLand("546") shouldBe LandkodeEnum.P_SIKKIM
        mapper.mapLand("556") shouldBe LandkodeEnum.P_YEMEN
        mapper.mapLand("669") shouldBe LandkodeEnum.P_PANAMAKANALSONEN
    }
})

fun mockMinimalSpec() = SimulerOffentligTjenestepensjonSpecV2(
    simuleringEtter2011 = SimuleringEtter2011SpecV2(
        simuleringType = SimuleringTypeSpecV2.ALDER_M_AFP_PRIVAT,
        fnr = Fnr("12345678901"),
        forsteUttakDato = LocalDate.of(2027, 2, 1),
    )
)

fun mockSpec(simuleringType: SimuleringTypeSpecV2) = SimulerOffentligTjenestepensjonSpecV2(
    simuleringEtter2011 = SimuleringEtter2011SpecV2(
        simuleringType = simuleringType, //hvis ALDER vil ikke afpOrdning bli satt
        fnr = Fnr("12345678901"),
        fnrAvdod = Fnr("10987654321"),
        fodselsar = 1960,
        ansettelsessektor = AnsattTypeCodeV2.OFFENTLIG,
        offentligAfpRett = true,
        privatAfpRett = false,
        simuleringsvalgOffentligAfp = null,
        samtykke = true,
        forventetInntekt = 1,
        antArInntektOverG = 2,
        forsteUttakDato = LocalDate.of(2027, 2, 1),
        utg = UttaksgradSpecV2.P_100,
        inntektUnderGradertUttak = 3,
        heltUttakDato = null,
        inntektEtterHeltUttak = 4,
        antallArInntektEtterHeltUttak = 5,
        utenlandsopphold = 6,
        flyktning = true,
        sivilstatus = SivilstatusSpecV2.GIFT,
        epsPensjon = true,
        eps2G = true,
        afpOrdning = AfpOrdningTypeSpecV2.AFPSTAT,
        afpInntektMndForUttak = true,
        brukerRegTPListe = emptyList(),
        stillingsprosentOffHeltUttak = StillingsprOffCodeV2.P_0,
        stillingsprosentOffGradertUttak = StillingsprOffCodeV2.P_100,
        dodsdato = LocalDate.of(2025, 2, 3),
        avdodAntallArIUtlandet = 6,
        avdodInntektForDod = 7,
        inntektAvdodOver1G = true,
        avdodMedlemAvFolketrygden = true,
        avdodFlyktning = true,
        simulerForTp = null,
        utenlandsperiodeForSimuleringList = listOf(
            UtenlandsperiodeForSimuleringV2(
                periodeFom = LocalDate.now().minusYears(2),
                periodeTom = LocalDate.now().minusYears(1),
                land = "SWE",
                arbeidetIUtland = true,
            ),
            UtenlandsperiodeForSimuleringV2(
                periodeFom = LocalDate.now().minusYears(5),
                periodeTom = LocalDate.now().minusYears(2),
                land = "CAN",
                arbeidetIUtland = true,
            )
        ),
        fremtidigInntektList = emptyList(),
    )
)
