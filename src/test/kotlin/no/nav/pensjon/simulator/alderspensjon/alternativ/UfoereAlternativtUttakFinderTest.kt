package no.nav.pensjon.simulator.alderspensjon.alternativ

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.UttakAlderDiscriminator
import no.nav.pensjon.simulator.core.exception.InvalidArgumentException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligOpptjeningException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligTrygdetidException
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import java.time.LocalDate

class UfoereAlternativtUttakFinderTest : FunSpec({

    val pid = Pid("12345678910")
    val foedselsdato = LocalDate.of(1963, 1, 15)
    val normalder = Alder(67, 0)
    val today = LocalDate.of(2024, 6, 1)

    fun discriminator(
        simulerBehaviour: (Any) -> SimulatorOutput = { SimulatorOutput() }
    ): UttakAlderDiscriminator = mockk {
        every { fetchFoedselsdato(pid) } returns foedselsdato
        every { simuler(any()) } answers { simulerBehaviour(firstArg()) }
    }

    fun normalderService(): NormertPensjonsalderService = mockk {
        every { normalder(foedselsdato) } returns normalder
    }

    fun finder(
        discriminator: UttakAlderDiscriminator = discriminator(),
        spec: no.nav.pensjon.simulator.core.spec.SimuleringSpec = simuleringSpec()
    ) = UfoereAlternativtUttakFinder(discriminator, spec, normalderService()) { today }

    // --- Happy path: all simulations succeed ---

    test("findAlternativtUttak returns GOOD when simulation succeeds at smallest index") {
        val result = finder().findAlternativtUttak(
            foersteUttakAlder = Alder(62, 0),
            andreUttakMinAlder = Alder(67, 0),
            andreUttakMaxAlder = Alder(67, 0),
            maxUttaksgrad = UttakGradKode.P_80,
            keepUttaksgradConstant = true
        )

        result.alternativ.shouldNotBeNull()
        result.alternativ.resultStatus shouldBe SimulatorResultStatus.GOOD
    }

    test("findAlternativtUttak returns correct gradert uttak alder when succeeding") {
        val result = finder().findAlternativtUttak(
            foersteUttakAlder = Alder(62, 0),
            andreUttakMinAlder = Alder(67, 0),
            andreUttakMaxAlder = Alder(67, 0),
            maxUttaksgrad = UttakGradKode.P_80,
            keepUttaksgradConstant = true
        )

        val gradert = result.alternativ!!.gradertUttakAlder!!
        gradert.alder shouldBe Alder(62, 0)
        gradert.uttakDato shouldBe LocalDate.of(2025, 2, 1) // foedselsdato + 62y + 1m, first of month
    }

    test("findAlternativtUttak returns correct helt uttak alder when succeeding") {
        val result = finder().findAlternativtUttak(
            foersteUttakAlder = Alder(62, 0),
            andreUttakMinAlder = Alder(67, 0),
            andreUttakMaxAlder = Alder(67, 0),
            maxUttaksgrad = UttakGradKode.P_80,
            keepUttaksgradConstant = true
        )

        val helt = result.alternativ!!.heltUttakAlder
        helt.alder shouldBe Alder(67, 0)
        helt.uttakDato shouldBe LocalDate.of(2030, 2, 1)
    }

    test("findAlternativtUttak uses maxUttaksgrad when keepUttaksgradConstant is true") {
        val result = finder().findAlternativtUttak(
            foersteUttakAlder = Alder(62, 0),
            andreUttakMinAlder = Alder(67, 0),
            andreUttakMaxAlder = Alder(67, 0),
            maxUttaksgrad = UttakGradKode.P_60,
            keepUttaksgradConstant = true
        )

        result.alternativ!!.uttakGrad shouldBe UttakGradKode.P_60
    }

    test("findAlternativtUttak returns non-null pensjon when simulation succeeds") {
        val result = finder().findAlternativtUttak(
            foersteUttakAlder = Alder(62, 0),
            andreUttakMinAlder = Alder(67, 0),
            andreUttakMaxAlder = Alder(67, 0),
            maxUttaksgrad = UttakGradKode.P_80,
            keepUttaksgradConstant = true
        )

        result.pensjon.shouldNotBeNull()
    }

    // --- All simulations fail ---

    test("findAlternativtUttak returns BAD when all simulations fail with UtilstrekkeligOpptjeningException") {
        val disc = discriminator { throw UtilstrekkeligOpptjeningException("test") }

        val result = finder(discriminator = disc).findAlternativtUttak(
            foersteUttakAlder = Alder(62, 0),
            andreUttakMinAlder = Alder(67, 0),
            andreUttakMaxAlder = Alder(67, 0),
            maxUttaksgrad = UttakGradKode.P_80,
            keepUttaksgradConstant = true
        )

        result.alternativ!!.resultStatus shouldBe SimulatorResultStatus.BAD
    }

    test("findAlternativtUttak returns BAD when all simulations fail with UtilstrekkeligTrygdetidException") {
        val disc = discriminator { throw UtilstrekkeligTrygdetidException() }

        val result = finder(discriminator = disc).findAlternativtUttak(
            foersteUttakAlder = Alder(62, 0),
            andreUttakMinAlder = Alder(67, 0),
            andreUttakMaxAlder = Alder(67, 0),
            maxUttaksgrad = UttakGradKode.P_80,
            keepUttaksgradConstant = true
        )

        result.alternativ!!.resultStatus shouldBe SimulatorResultStatus.BAD
    }

    test("findAlternativtUttak uses default parameters when all simulations fail") {
        val disc = discriminator { throw UtilstrekkeligOpptjeningException("test") }

        val result = finder(discriminator = disc).findAlternativtUttak(
            foersteUttakAlder = Alder(62, 0),
            andreUttakMinAlder = Alder(67, 0),
            andreUttakMaxAlder = Alder(67, 0),
            maxUttaksgrad = UttakGradKode.P_80,
            keepUttaksgradConstant = true
        )

        val alternativ = result.alternativ!!
        // Default: uttakGrad = P_20, gradert uttak 1 month before normalder, helt uttak at normalder
        alternativ.uttakGrad shouldBe UttakGradKode.P_20
        alternativ.gradertUttakAlder!!.alder shouldBe Alder(66, 11)
        alternativ.heltUttakAlder.alder shouldBe Alder(67, 0)
    }

    test("findAlternativtUttak returns null pensjon when all simulations fail") {
        val disc = discriminator { throw UtilstrekkeligOpptjeningException("test") }

        val result = finder(discriminator = disc).findAlternativtUttak(
            foersteUttakAlder = Alder(62, 0),
            andreUttakMinAlder = Alder(67, 0),
            andreUttakMaxAlder = Alder(67, 0),
            maxUttaksgrad = UttakGradKode.P_80,
            keepUttaksgradConstant = true
        )

        result.pensjon.shouldBeNull()
    }

    // --- SUBOPTIMAL: uttaksgrad transition ---

    test("findAlternativtUttak returns SUBOPTIMAL when search finds result with uttaksgrad transition") {
        val disc = discriminator { spec ->
            val simSpec = spec as no.nav.pensjon.simulator.core.spec.SimuleringSpec
            if (simSpec.uttakGrad == UttakGradKode.P_80) throw UtilstrekkeligOpptjeningException("test")
            SimulatorOutput()
        }

        val result = finder(discriminator = disc).findAlternativtUttak(
            foersteUttakAlder = Alder(62, 0),
            andreUttakMinAlder = Alder(67, 0),
            andreUttakMaxAlder = Alder(67, 0),
            maxUttaksgrad = UttakGradKode.P_80,
            keepUttaksgradConstant = false
        )

        result.alternativ!!.resultStatus shouldBe SimulatorResultStatus.SUBOPTIMAL
        result.alternativ.uttakGrad shouldBe UttakGradKode.P_60
    }

    // --- keepUttaksgradConstant = false ---

    test("findAlternativtUttak uses indexed uttaksgrad submap when keepUttaksgradConstant is false") {
        val result = finder().findAlternativtUttak(
            foersteUttakAlder = Alder(62, 0),
            andreUttakMinAlder = Alder(67, 0),
            andreUttakMaxAlder = Alder(67, 0),
            maxUttaksgrad = UttakGradKode.P_80,
            keepUttaksgradConstant = false
        )

        // When all simulations succeed, smallest index (0) maps to P_80 (highest in submap)
        result.alternativ!!.uttakGrad shouldBe UttakGradKode.P_80
        result.alternativ.resultStatus shouldBe SimulatorResultStatus.GOOD
    }

    // --- Multiple andreUttak alder values ---

    test("findAlternativtUttak handles range of andreUttak alder values") {
        val result = finder().findAlternativtUttak(
            foersteUttakAlder = Alder(62, 0),
            andreUttakMinAlder = Alder(67, 0),
            andreUttakMaxAlder = Alder(67, 6),
            maxUttaksgrad = UttakGradKode.P_50,
            keepUttaksgradConstant = true
        )

        result.alternativ.shouldNotBeNull()
        result.alternativ.resultStatus shouldBe SimulatorResultStatus.GOOD
        // With all succeeding, smallest index wins → earliest heltUttakFom
        result.alternativ.heltUttakAlder.alder shouldBe Alder(67, 0)
    }

    // --- onlyVilkaarsproeving ---

    test("findAlternativtUttak returns null pensjon when onlyVilkaarsproeving is true") {
        val spec = simuleringSpec().let {
            no.nav.pensjon.simulator.core.spec.SimuleringSpec(
                type = it.type,
                sivilstatus = it.sivilstatus,
                epsHarPensjon = it.epsHarPensjon,
                foersteUttakDato = it.foersteUttakDato,
                heltUttakDato = it.heltUttakDato,
                pid = it.pid,
                foedselDato = it.foedselDato,
                avdoed = it.avdoed,
                isTpOrigSimulering = it.isTpOrigSimulering,
                simulerForTp = it.simulerForTp,
                uttakGrad = it.uttakGrad,
                forventetInntektBeloep = it.forventetInntektBeloep,
                inntektUnderGradertUttakBeloep = it.inntektUnderGradertUttakBeloep,
                inntektEtterHeltUttakBeloep = it.inntektEtterHeltUttakBeloep,
                inntektEtterHeltUttakAntallAar = it.inntektEtterHeltUttakAntallAar,
                foedselAar = it.foedselAar,
                utlandAntallAar = it.utlandAntallAar,
                utlandPeriodeListe = it.utlandPeriodeListe,
                fremtidigInntektListe = it.fremtidigInntektListe,
                brukFremtidigInntekt = it.brukFremtidigInntekt,
                inntektOver1GAntallAar = it.inntektOver1GAntallAar,
                flyktning = it.flyktning,
                epsHarInntektOver2G = it.epsHarInntektOver2G,
                livsvarigOffentligAfp = it.livsvarigOffentligAfp,
                pre2025OffentligAfp = it.pre2025OffentligAfp,
                erAnonym = it.erAnonym,
                ignoreAvslag = it.ignoreAvslag,
                isHentPensjonsbeholdninger = it.isHentPensjonsbeholdninger,
                isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = it.isOutputSimulertBeregningsinformasjonForAllKnekkpunkter,
                onlyVilkaarsproeving = true,
                epsKanOverskrives = it.epsKanOverskrives
            )
        }

        val result = finder(spec = spec).findAlternativtUttak(
            foersteUttakAlder = Alder(62, 0),
            andreUttakMinAlder = Alder(67, 0),
            andreUttakMaxAlder = Alder(67, 0),
            maxUttaksgrad = UttakGradKode.P_80,
            keepUttaksgradConstant = true
        )

        result.pensjon.shouldBeNull()
        result.alternativ!!.resultStatus shouldBe SimulatorResultStatus.GOOD
    }

    // --- PID null ---

    test("findAlternativtUttak throws InvalidArgumentException when PID is null") {
        val spec = simuleringSpec().let {
            no.nav.pensjon.simulator.core.spec.SimuleringSpec(
                type = it.type,
                sivilstatus = it.sivilstatus,
                epsHarPensjon = it.epsHarPensjon,
                foersteUttakDato = it.foersteUttakDato,
                heltUttakDato = it.heltUttakDato,
                pid = null,
                foedselDato = it.foedselDato,
                avdoed = it.avdoed,
                isTpOrigSimulering = it.isTpOrigSimulering,
                simulerForTp = it.simulerForTp,
                uttakGrad = it.uttakGrad,
                forventetInntektBeloep = it.forventetInntektBeloep,
                inntektUnderGradertUttakBeloep = it.inntektUnderGradertUttakBeloep,
                inntektEtterHeltUttakBeloep = it.inntektEtterHeltUttakBeloep,
                inntektEtterHeltUttakAntallAar = it.inntektEtterHeltUttakAntallAar,
                foedselAar = it.foedselAar,
                utlandAntallAar = it.utlandAntallAar,
                utlandPeriodeListe = it.utlandPeriodeListe,
                fremtidigInntektListe = it.fremtidigInntektListe,
                brukFremtidigInntekt = it.brukFremtidigInntekt,
                inntektOver1GAntallAar = it.inntektOver1GAntallAar,
                flyktning = it.flyktning,
                epsHarInntektOver2G = it.epsHarInntektOver2G,
                livsvarigOffentligAfp = it.livsvarigOffentligAfp,
                pre2025OffentligAfp = it.pre2025OffentligAfp,
                erAnonym = true,
                ignoreAvslag = it.ignoreAvslag,
                isHentPensjonsbeholdninger = it.isHentPensjonsbeholdninger,
                isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = it.isOutputSimulertBeregningsinformasjonForAllKnekkpunkter,
                onlyVilkaarsproeving = it.onlyVilkaarsproeving,
                epsKanOverskrives = it.epsKanOverskrives
            )
        }

        shouldThrow<InvalidArgumentException> {
            finder(spec = spec).findAlternativtUttak(
                foersteUttakAlder = Alder(62, 0),
                andreUttakMinAlder = Alder(67, 0),
                andreUttakMaxAlder = Alder(67, 0),
                maxUttaksgrad = UttakGradKode.P_80,
                keepUttaksgradConstant = true
            )
        }
    }

    // --- Alder with months ---

    test("findAlternativtUttak calculates correct ages when foersteUttakAlder has months") {
        val result = finder().findAlternativtUttak(
            foersteUttakAlder = Alder(62, 6),
            andreUttakMinAlder = Alder(67, 0),
            andreUttakMaxAlder = Alder(67, 0),
            maxUttaksgrad = UttakGradKode.P_50,
            keepUttaksgradConstant = true
        )

        val gradert = result.alternativ!!.gradertUttakAlder!!
        gradert.alder shouldBe Alder(62, 6)
        // foedselsdato(1963-01-15) + 62y6m = 2025-07-15, +1m = 2025-08-15, withDayOfMonth(1) = 2025-08-01
        gradert.uttakDato shouldBe LocalDate.of(2025, 8, 1)
    }

    // --- Default parameters date calculations ---

    test("findAlternativtUttak default gradert uttak is 1 month before normalder") {
        val disc = discriminator { throw UtilstrekkeligOpptjeningException("test") }

        val result = finder(discriminator = disc).findAlternativtUttak(
            foersteUttakAlder = Alder(62, 0),
            andreUttakMinAlder = Alder(67, 0),
            andreUttakMaxAlder = Alder(67, 0),
            maxUttaksgrad = UttakGradKode.P_80,
            keepUttaksgradConstant = true
        )

        val gradert = result.alternativ!!.gradertUttakAlder!!
        // normalder(67,0).minusMaaneder(1) = Alder(66,11)
        // uttakDato(1963-01-15, Alder(66,11)) = 1963-01-15 + 66y + 12m, withDayOfMonth(1) = 2030-01-01
        gradert.alder shouldBe Alder(66, 11)
        gradert.uttakDato shouldBe LocalDate.of(2030, 1, 1)
    }

    test("findAlternativtUttak default helt uttak is at normalder") {
        val disc = discriminator { throw UtilstrekkeligOpptjeningException("test") }

        val result = finder(discriminator = disc).findAlternativtUttak(
            foersteUttakAlder = Alder(62, 0),
            andreUttakMinAlder = Alder(67, 0),
            andreUttakMaxAlder = Alder(67, 0),
            maxUttaksgrad = UttakGradKode.P_80,
            keepUttaksgradConstant = true
        )

        val helt = result.alternativ!!.heltUttakAlder
        helt.alder shouldBe Alder(67, 0)
        helt.uttakDato shouldBe LocalDate.of(2030, 2, 1)
    }
})
