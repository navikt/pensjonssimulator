package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.inntekt.InntektService
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3.*
import java.time.LocalDate

class SimulerOffentligTjenestepensjonMapperV3Test : FunSpec({

    val pidValue = "12345678910"
    val foedselsdato = LocalDate.of(1963, 1, 1)
    val fnr = Fnr(pid = pidValue)

    fun mapper(
        personService: GeneralPersonService = mockk {
            every { foedselsdato(Pid(pidValue)) } returns foedselsdato
        },
        inntektService: InntektService = mockk()
    ) = SimulerOffentligTjenestepensjonMapperV3(personService, inntektService)

    fun simuleringEtter2011(
        simuleringType: SimuleringTypeSpecV3 = SimuleringTypeSpecV3.ALDER,
        fnrParam: Fnr = fnr,
        sivilstatus: SivilstatusSpecV3? = null,
        epsPensjon: Boolean? = null,
        eps2G: Boolean? = null,
        forsteUttakDato: LocalDate? = null,
        heltUttakDato: LocalDate? = null,
        utg: UttaksgradSpecV3? = null,
        forventetInntekt: Int? = null,
        inntektUnderGradertUttak: Int? = null,
        inntektEtterHeltUttak: Int = 0,
        antallArInntektEtterHeltUttak: Int = 0,
        fodselsar: Int? = null,
        utenlandsopphold: Int? = null,
        flyktning: Boolean? = null,
        afpOrdning: AfpOrdningTypeSpecV3? = null,
        afpInntektMndForUttak: Boolean? = null,
        fnrAvdod: Fnr? = null,
        dodsdato: LocalDate? = null,
        avdodAntallArIUtlandet: Int? = null,
        avdodInntektForDod: Int? = null,
        inntektAvdodOver1G: Boolean? = null,
        avdodMedlemAvFolketrygden: Boolean? = null,
        utenlandsperiodeForSimuleringList: List<UtenlandsperiodeForSimuleringV3> = emptyList(),
        stillingsprosentOffHeltUttak: StillingsprOffCodeV3? = null,
        stillingsprosentOffGradertUttak: StillingsprOffCodeV3? = null
    ) = SimuleringEtter2011SpecV3(
        simuleringType = simuleringType,
        fnr = fnrParam,
        sivilstatus = sivilstatus,
        epsPensjon = epsPensjon,
        eps2G = eps2G,
        forsteUttakDato = forsteUttakDato,
        heltUttakDato = heltUttakDato,
        utg = utg,
        forventetInntekt = forventetInntekt,
        inntektUnderGradertUttak = inntektUnderGradertUttak,
        inntektEtterHeltUttak = inntektEtterHeltUttak,
        antallArInntektEtterHeltUttak = antallArInntektEtterHeltUttak,
        fodselsar = fodselsar,
        utenlandsopphold = utenlandsopphold,
        flyktning = flyktning,
        afpOrdning = afpOrdning,
        afpInntektMndForUttak = afpInntektMndForUttak,
        fnrAvdod = fnrAvdod,
        dodsdato = dodsdato,
        avdodAntallArIUtlandet = avdodAntallArIUtlandet,
        avdodInntektForDod = avdodInntektForDod,
        inntektAvdodOver1G = inntektAvdodOver1G,
        avdodMedlemAvFolketrygden = avdodMedlemAvFolketrygden,
        utenlandsperiodeForSimuleringList = utenlandsperiodeForSimuleringList,
        stillingsprosentOffHeltUttak = stillingsprosentOffHeltUttak,
        stillingsprosentOffGradertUttak = stillingsprosentOffGradertUttak
    )

    fun specV3(source: SimuleringEtter2011SpecV3 = simuleringEtter2011()) =
        SimulerOffentligTjenestepensjonSpecV3(simuleringEtter2011 = source)

    // --- fromDto: full mapping ---

    test("fromDto maps all fields from fully populated source") {
        val source = simuleringEtter2011(
            simuleringType = SimuleringTypeSpecV3.ALDER_M_AFP_PRIVAT,
            sivilstatus = SivilstatusSpecV3.GIFT,
            epsPensjon = true,
            eps2G = true,
            forsteUttakDato = LocalDate.of(2029, 1, 1),
            heltUttakDato = LocalDate.of(2030, 6, 1),
            utg = UttaksgradSpecV3.P_50,
            forventetInntekt = 500000,
            inntektUnderGradertUttak = 250000,
            inntektEtterHeltUttak = 100000,
            antallArInntektEtterHeltUttak = 3,
            fodselsar = 1963,
            utenlandsopphold = 5,
            flyktning = false
        )

        val result = mapper().fromDto(specV3(source))

        result.type shouldBe SimuleringTypeEnum.ALDER_M_AFP_PRIVAT
        result.sivilstatus shouldBe SivilstatusType.GIFT
        result.epsHarPensjon shouldBe true
        result.epsHarInntektOver2G shouldBe true
        result.foersteUttakDato shouldBe LocalDate.of(2029, 1, 1)
        result.heltUttakDato shouldBe LocalDate.of(2030, 6, 1)
        result.uttakGrad shouldBe UttakGradKode.P_50
        result.pid shouldBe Pid(pidValue)
        result.foedselDato shouldBe foedselsdato
        result.forventetInntektBeloep shouldBe 500000
        result.inntektUnderGradertUttakBeloep shouldBe 250000
        result.inntektEtterHeltUttakBeloep shouldBe 100000
        result.inntektEtterHeltUttakAntallAar shouldBe 3
        result.foedselAar shouldBe 1963
        result.utlandAntallAar shouldBe 5
        result.flyktning shouldBe false
    }

    // --- fromDto: default values ---

    test("fromDto defaults sivilstatus to UGIF when null") {
        val result = mapper().fromDto(specV3(simuleringEtter2011(sivilstatus = null)))

        result.sivilstatus shouldBe SivilstatusType.UGIF
    }

    test("fromDto defaults uttakGrad to P_100 when utg is null") {
        val result = mapper().fromDto(specV3(simuleringEtter2011(utg = null)))

        result.uttakGrad shouldBe UttakGradKode.P_100
    }

    test("fromDto defaults forventetInntekt to 0 when null") {
        val result = mapper().fromDto(specV3(simuleringEtter2011(forventetInntekt = null)))

        result.forventetInntektBeloep shouldBe 0
    }

    test("fromDto defaults inntektUnderGradertUttak to 0 when null") {
        val result = mapper().fromDto(specV3(simuleringEtter2011(inntektUnderGradertUttak = null)))

        result.inntektUnderGradertUttakBeloep shouldBe 0
    }

    test("fromDto defaults fodselsar to 0 when null") {
        val result = mapper().fromDto(specV3(simuleringEtter2011(fodselsar = null)))

        result.foedselAar shouldBe 0
    }

    test("fromDto defaults utenlandsopphold to 0 when null") {
        val result = mapper().fromDto(specV3(simuleringEtter2011(utenlandsopphold = null)))

        result.utlandAntallAar shouldBe 0
    }

    // --- fromDto: boolean fields ---

    test("fromDto maps epsPensjon=false to epsHarPensjon=false") {
        val result = mapper().fromDto(specV3(simuleringEtter2011(epsPensjon = false)))

        result.epsHarPensjon shouldBe false
    }

    test("fromDto maps epsPensjon=null to epsHarPensjon=false") {
        val result = mapper().fromDto(specV3(simuleringEtter2011(epsPensjon = null)))

        result.epsHarPensjon shouldBe false
    }

    test("fromDto maps eps2G=false to epsHarInntektOver2G=false") {
        val result = mapper().fromDto(specV3(simuleringEtter2011(eps2G = false)))

        result.epsHarInntektOver2G shouldBe false
    }

    test("fromDto maps eps2G=null to epsHarInntektOver2G=false") {
        val result = mapper().fromDto(specV3(simuleringEtter2011(eps2G = null)))

        result.epsHarInntektOver2G shouldBe false
    }

    // --- fromDto: fixed values ---

    test("fromDto sets fixed values correctly") {
        val result = mapper().fromDto(specV3())

        result.isTpOrigSimulering shouldBe false
        result.simulerForTp shouldBe false
        result.fremtidigInntektListe shouldBe mutableListOf()
        result.brukFremtidigInntekt shouldBe false
        result.inntektOver1GAntallAar shouldBe 0
        result.livsvarigOffentligAfp shouldBe null
        result.erAnonym shouldBe false
        result.ignoreAvslag shouldBe false
        result.isHentPensjonsbeholdninger shouldBe true
        result.isOutputSimulertBeregningsinformasjonForAllKnekkpunkter shouldBe true
        result.onlyVilkaarsproeving shouldBe false
        result.epsKanOverskrives shouldBe false
    }

    // --- fromDto: simuleringType mapping ---

    test("fromDto maps ALDER simuleringType") {
        val result = mapper().fromDto(specV3(simuleringEtter2011(simuleringType = SimuleringTypeSpecV3.ALDER)))

        result.type shouldBe SimuleringTypeEnum.ALDER
    }

    test("fromDto maps AFP_ETTERF_ALDER simuleringType") {
        val inntektService = mockk<InntektService> {
            every { hentSisteMaanedsInntektOver1G(any()) } returns 42
        }
        val source = simuleringEtter2011(
            simuleringType = SimuleringTypeSpecV3.AFP_ETTERF_ALDER,
            afpOrdning = AfpOrdningTypeSpecV3.AFPSTAT
        )

        val result = mapper(inntektService = inntektService).fromDto(specV3(source))

        result.type shouldBe SimuleringTypeEnum.AFP_ETTERF_ALDER
    }

    test("fromDto maps ALDER_M_GJEN simuleringType") {
        val avdodPid = "98765432100"
        val avdodFnr = Fnr(pid = avdodPid)
        val personService = mockk<GeneralPersonService> {
            every { foedselsdato(Pid(pidValue)) } returns foedselsdato
        }
        val source = simuleringEtter2011(
            simuleringType = SimuleringTypeSpecV3.ALDER_M_GJEN,
            fnrAvdod = avdodFnr,
            dodsdato = LocalDate.of(2028, 5, 15),
            avdodAntallArIUtlandet = 2,
            avdodInntektForDod = 400000,
            avdodMedlemAvFolketrygden = true,
            inntektAvdodOver1G = true
        )

        val result = mapper(personService = personService).fromDto(specV3(source))

        result.type shouldBe SimuleringTypeEnum.ALDER_M_GJEN
    }

    // --- fromDto: utenlandsperioder ---

    test("fromDto maps utenlandsperioder") {
        val perioder = listOf(
            UtenlandsperiodeForSimuleringV3(
                land = "SWE",
                arbeidetIUtland = true,
                periodeFom = LocalDate.of(2010, 1, 1),
                periodeTom = LocalDate.of(2015, 12, 31)
            ),
            UtenlandsperiodeForSimuleringV3(
                land = "DEU",
                arbeidetIUtland = false,
                periodeFom = LocalDate.of(2016, 1, 1),
                periodeTom = null
            )
        )

        val result = mapper().fromDto(specV3(simuleringEtter2011(utenlandsperiodeForSimuleringList = perioder)))

        result.utlandPeriodeListe.size shouldBe 2
        result.utlandPeriodeListe[0].land shouldBe LandkodeEnum.SWE
        result.utlandPeriodeListe[0].arbeidet shouldBe true
        result.utlandPeriodeListe[0].fom shouldBe LocalDate.of(2010, 1, 1)
        result.utlandPeriodeListe[0].tom shouldBe LocalDate.of(2015, 12, 31)
        result.utlandPeriodeListe[1].land shouldBe LandkodeEnum.DEU
        result.utlandPeriodeListe[1].arbeidet shouldBe false
        result.utlandPeriodeListe[1].tom shouldBe null
    }

    test("fromDto maps empty utenlandsperioder to empty list") {
        val result = mapper().fromDto(specV3(simuleringEtter2011(utenlandsperiodeForSimuleringList = emptyList())))

        result.utlandPeriodeListe shouldBe mutableListOf()
    }

    // --- avdoed ---

    test("avdoed maps all fields when fnrAvdod is present") {
        val avdodPid = "98765432100"
        val source = simuleringEtter2011(
            fnrAvdod = Fnr(pid = avdodPid),
            dodsdato = LocalDate.of(2028, 5, 15),
            avdodAntallArIUtlandet = 3,
            avdodInntektForDod = 400000,
            avdodMedlemAvFolketrygden = true,
            inntektAvdodOver1G = true
        )

        val result = mapper().avdoed(source)!!

        result.pid shouldBe Pid(avdodPid)
        result.doedDato shouldBe LocalDate.of(2028, 5, 15)
        result.antallAarUtenlands shouldBe 3
        result.inntektFoerDoed shouldBe 400000
        result.erMedlemAvFolketrygden shouldBe true
        result.harInntektOver1G shouldBe true
    }

    test("avdoed returns null when fnrAvdod is null") {
        val result = mapper().avdoed(simuleringEtter2011(fnrAvdod = null))

        result shouldBe null
    }

    test("avdoed defaults antallAarUtenlands to 0 when null") {
        val source = simuleringEtter2011(
            fnrAvdod = Fnr(pid = "98765432100"),
            dodsdato = LocalDate.of(2028, 1, 1),
            avdodAntallArIUtlandet = null
        )

        val result = mapper().avdoed(source)!!

        result.antallAarUtenlands shouldBe 0
    }

    test("avdoed defaults inntektFoerDoed to 0 when null") {
        val source = simuleringEtter2011(
            fnrAvdod = Fnr(pid = "98765432100"),
            dodsdato = LocalDate.of(2028, 1, 1),
            avdodInntektForDod = null
        )

        val result = mapper().avdoed(source)!!

        result.inntektFoerDoed shouldBe 0
    }

    test("avdoed maps avdodMedlemAvFolketrygden=null to false") {
        val source = simuleringEtter2011(
            fnrAvdod = Fnr(pid = "98765432100"),
            dodsdato = LocalDate.of(2028, 1, 1),
            avdodMedlemAvFolketrygden = null
        )

        val result = mapper().avdoed(source)!!

        result.erMedlemAvFolketrygden shouldBe false
    }

    test("avdoed maps inntektAvdodOver1G=null to false") {
        val source = simuleringEtter2011(
            fnrAvdod = Fnr(pid = "98765432100"),
            dodsdato = LocalDate.of(2028, 1, 1),
            inntektAvdodOver1G = null
        )

        val result = mapper().avdoed(source)!!

        result.harInntektOver1G shouldBe false
    }

    // --- pre2025OffentligAfpSpec ---

    test("pre2025OffentligAfpSpec returns spec when simuleringType is AFP_ETTERF_ALDER") {
        val inntektService = mockk<InntektService> {
            every { hentSisteMaanedsInntektOver1G(true) } returns 20000
        }
        val source = simuleringEtter2011(
            simuleringType = SimuleringTypeSpecV3.AFP_ETTERF_ALDER,
            afpOrdning = AfpOrdningTypeSpecV3.AFPSTAT,
            afpInntektMndForUttak = true,
            inntektUnderGradertUttak = 150000
        )

        val result = mapper(inntektService = inntektService).pre2025OffentligAfpSpec(source)!!

        result.afpOrdning shouldBe AFPtypeEnum.AFPSTAT
        result.inntektMaanedenFoerAfpUttakBeloep shouldBe 20000
        result.inntektUnderAfpUttakBeloep shouldBe 150000
    }

    test("pre2025OffentligAfpSpec returns null when simuleringType is not AFP_ETTERF_ALDER") {
        val source = simuleringEtter2011(simuleringType = SimuleringTypeSpecV3.ALDER)

        val result = mapper().pre2025OffentligAfpSpec(source)

        result shouldBe null
    }

    test("pre2025OffentligAfpSpec defaults inntektMaanedenFoerAfpUttakBeloep to 0 when afpInntektMndForUttak is null") {
        val source = simuleringEtter2011(
            simuleringType = SimuleringTypeSpecV3.AFP_ETTERF_ALDER,
            afpOrdning = AfpOrdningTypeSpecV3.LONHO,
            afpInntektMndForUttak = null
        )

        val result = mapper().pre2025OffentligAfpSpec(source)!!

        result.inntektMaanedenFoerAfpUttakBeloep shouldBe 0
    }

    test("pre2025OffentligAfpSpec defaults inntektUnderAfpUttakBeloep to 0 when inntektUnderGradertUttak is null") {
        val inntektService = mockk<InntektService> {
            every { hentSisteMaanedsInntektOver1G(any()) } returns 42
        }
        val source = simuleringEtter2011(
            simuleringType = SimuleringTypeSpecV3.AFP_ETTERF_ALDER,
            afpOrdning = AfpOrdningTypeSpecV3.AFPKOM,
            afpInntektMndForUttak = false,
            inntektUnderGradertUttak = null
        )

        val result = mapper(inntektService = inntektService).pre2025OffentligAfpSpec(source)!!

        result.inntektUnderAfpUttakBeloep shouldBe 0
    }

    // --- mapLand ---

    test("mapLand maps standard land code via LandkodeEnum valueOf") {
        val result = mapper().mapLand("SWE")

        result shouldBe LandkodeEnum.SWE
    }

    test("mapLand maps irregular code '???' to P_UKJENT") {
        val result = mapper().mapLand("???")

        result shouldBe LandkodeEnum.P_UKJENT
    }

    test("mapLand maps irregular code '349' to P_SPANSKE_OMR_AFRIKA") {
        val result = mapper().mapLand("349")

        result shouldBe LandkodeEnum.P_SPANSKE_OMR_AFRIKA
    }

    test("mapLand maps irregular code '546' to P_SIKKIM") {
        val result = mapper().mapLand("546")

        result shouldBe LandkodeEnum.P_SIKKIM
    }

    test("mapLand maps irregular code '556' to P_YEMEN") {
        val result = mapper().mapLand("556")

        result shouldBe LandkodeEnum.P_YEMEN
    }

    test("mapLand maps irregular code '669' to P_PANAMAKANALSONEN") {
        val result = mapper().mapLand("669")

        result shouldBe LandkodeEnum.P_PANAMAKANALSONEN
    }

    // --- stillingsprosentFromDto ---

    test("stillingsprosentFromDto maps stillingsprosent codes") {
        val source = simuleringEtter2011(
            stillingsprosentOffHeltUttak = StillingsprOffCodeV3.P_100,
            stillingsprosentOffGradertUttak = StillingsprOffCodeV3.P_50
        )

        val result = mapper().stillingsprosentFromDto(specV3(source))

        result.stillingsprosentOffHeltUttak shouldBe 100
        result.stillingsprosentOffGradertUttak shouldBe 50
    }

    test("stillingsprosentFromDto maps null codes to null") {
        val source = simuleringEtter2011(
            stillingsprosentOffHeltUttak = null,
            stillingsprosentOffGradertUttak = null
        )

        val result = mapper().stillingsprosentFromDto(specV3(source))

        result.stillingsprosentOffHeltUttak shouldBe null
        result.stillingsprosentOffGradertUttak shouldBe null
    }
})
