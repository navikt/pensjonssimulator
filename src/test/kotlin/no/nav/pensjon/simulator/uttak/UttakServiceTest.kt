package no.nav.pensjon.simulator.uttak

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertAlternativ
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertPensjonEllerAlternativ
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertUttakAlder
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimuleringFacade
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulatorResultStatus
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import java.time.LocalDate

class UttakServiceTest : FunSpec({

    val foedselsdato = LocalDate.of(1963, 1, 1)
    val nedreAlder = Alder(62, 0)
    // PensjonAlderDato(foedselsdato=1963-01-01, alder=Alder(62, 0))
    // dato = 1963-01-01 + 62 years, withDayOfMonth(1), plusMonths(1) = 2025-02-01
    val alderDato = LocalDate.of(2025, 2, 1)
    val ingenAlternativ = SimulertPensjonEllerAlternativ(pensjon = null, alternativ = null)

    test("finnTidligstMuligUttak returns tidligst uttak date when alder date is in the future and no alternativ") {
        val normalderService = mockk<NormertPensjonsalderService>()
        val simuleringFacade = mockk<SimuleringFacade>()
        every { normalderService.nedreAlder(foedselsdato) } returns nedreAlder
        every { simuleringFacade.simulerAlderspensjon(any(), inkluderPensjonHvisUbetinget = true) } returns ingenAlternativ

        val result = UttakService(simuleringFacade, normalderService) { LocalDate.of(2024, 1, 1) }
            .finnTidligstMuligUttak(simuleringSpec(foedselsdato = foedselsdato))

        result.uttakDato shouldBe alderDato
        result.uttaksgrad shouldBe Uttaksgrad.FEMTI_PROSENT
        result.feil shouldBe null
    }

    test("finnTidligstMuligUttak adjusts date to first of next month when alder date is in the past") {
        val normalderService = mockk<NormertPensjonsalderService>()
        val simuleringFacade = mockk<SimuleringFacade>()
        every { normalderService.nedreAlder(foedselsdato) } returns nedreAlder
        every { simuleringFacade.simulerAlderspensjon(any(), inkluderPensjonHvisUbetinget = true) } returns ingenAlternativ

        val result = UttakService(simuleringFacade, normalderService) { LocalDate.of(2025, 3, 15) }
            .finnTidligstMuligUttak(simuleringSpec(foedselsdato = foedselsdato))

        result.uttakDato shouldBe LocalDate.of(2025, 4, 1)
    }

    test("finnTidligstMuligUttak adjusts date to first of next month when alder date equals today") {
        val normalderService = mockk<NormertPensjonsalderService>()
        val simuleringFacade = mockk<SimuleringFacade>()
        every { normalderService.nedreAlder(foedselsdato) } returns nedreAlder
        every { simuleringFacade.simulerAlderspensjon(any(), inkluderPensjonHvisUbetinget = true) } returns ingenAlternativ

        val result = UttakService(simuleringFacade, normalderService) { alderDato }
            .finnTidligstMuligUttak(simuleringSpec(foedselsdato = foedselsdato))

        result.uttakDato shouldBe LocalDate.of(2025, 3, 1)
    }

    test("finnTidligstMuligUttak passes adjusted date as foersteUttakDato to simulation") {
        val specSlot = slot<SimuleringSpec>()
        val normalderService = mockk<NormertPensjonsalderService>()
        val simuleringFacade = mockk<SimuleringFacade>()
        every { normalderService.nedreAlder(foedselsdato) } returns nedreAlder
        every {
            simuleringFacade.simulerAlderspensjon(capture(specSlot), inkluderPensjonHvisUbetinget = true)
        } returns ingenAlternativ

        UttakService(simuleringFacade, normalderService) { LocalDate.of(2025, 5, 10) }
            .finnTidligstMuligUttak(simuleringSpec(foedselsdato = foedselsdato))

        specSlot.captured.foersteUttakDato shouldBe LocalDate.of(2025, 6, 1)
    }

    test("finnTidligstMuligUttak passes unadjusted date as foersteUttakDato when alder date is in the future") {
        val specSlot = slot<SimuleringSpec>()
        val normalderService = mockk<NormertPensjonsalderService>()
        val simuleringFacade = mockk<SimuleringFacade>()
        every { normalderService.nedreAlder(foedselsdato) } returns nedreAlder
        every {
            simuleringFacade.simulerAlderspensjon(capture(specSlot), inkluderPensjonHvisUbetinget = true)
        } returns ingenAlternativ

        UttakService(simuleringFacade, normalderService) { LocalDate.of(2024, 1, 1) }
            .finnTidligstMuligUttak(simuleringSpec(foedselsdato = foedselsdato))

        specSlot.captured.foersteUttakDato shouldBe alderDato
    }

    test("finnTidligstMuligUttak uses gradertUttakAlder when alternativ has both gradert and helt uttak alder") {
        val gradertDato = LocalDate.of(2026, 8, 1)
        val normalderService = mockk<NormertPensjonsalderService>()
        val simuleringFacade = mockk<SimuleringFacade>()
        every { normalderService.nedreAlder(foedselsdato) } returns nedreAlder
        every { simuleringFacade.simulerAlderspensjon(any(), inkluderPensjonHvisUbetinget = true) } returns
                SimulertPensjonEllerAlternativ(
                    pensjon = null,
                    alternativ = SimulertAlternativ(
                        gradertUttakAlder = SimulertUttakAlder(Alder(63, 6), gradertDato),
                        uttakGrad = UttakGradKode.P_50,
                        heltUttakAlder = SimulertUttakAlder(Alder(67, 0), LocalDate.of(2030, 2, 1)),
                        resultStatus = SimulatorResultStatus.GOOD
                    )
                )

        val result = UttakService(simuleringFacade, normalderService) { LocalDate.of(2024, 1, 1) }
            .finnTidligstMuligUttak(simuleringSpec(foedselsdato = foedselsdato))

        result.uttakDato shouldBe gradertDato
    }

    test("finnTidligstMuligUttak uses heltUttakAlder when alternativ has no gradertUttakAlder") {
        val heltDato = LocalDate.of(2028, 2, 1)
        val normalderService = mockk<NormertPensjonsalderService>()
        val simuleringFacade = mockk<SimuleringFacade>()
        every { normalderService.nedreAlder(foedselsdato) } returns nedreAlder
        every { simuleringFacade.simulerAlderspensjon(any(), inkluderPensjonHvisUbetinget = true) } returns
                SimulertPensjonEllerAlternativ(
                    pensjon = null,
                    alternativ = SimulertAlternativ(
                        gradertUttakAlder = null,
                        uttakGrad = UttakGradKode.P_100,
                        heltUttakAlder = SimulertUttakAlder(Alder(65, 0), heltDato),
                        resultStatus = SimulatorResultStatus.GOOD
                    )
                )

        val result = UttakService(simuleringFacade, normalderService) { LocalDate.of(2024, 1, 1) }
            .finnTidligstMuligUttak(simuleringSpec(foedselsdato = foedselsdato))

        result.uttakDato shouldBe heltDato
    }

    test("finnTidligstMuligUttak derives uttaksgrad from spec uttakGrad") {
        val normalderService = mockk<NormertPensjonsalderService>()
        val simuleringFacade = mockk<SimuleringFacade>()
        every { normalderService.nedreAlder(foedselsdato) } returns nedreAlder
        every { simuleringFacade.simulerAlderspensjon(any(), inkluderPensjonHvisUbetinget = true) } returns ingenAlternativ

        // Default simuleringSpec uses UttakGradKode.P_50
        val result = UttakService(simuleringFacade, normalderService) { LocalDate.of(2024, 1, 1) }
            .finnTidligstMuligUttak(simuleringSpec(foedselsdato = foedselsdato))

        result.uttaksgrad shouldBe Uttaksgrad.FEMTI_PROSENT
    }

    test("finnTidligstMuligUttak returns null feil regardless of alternativ") {
        val normalderService = mockk<NormertPensjonsalderService>()
        val simuleringFacade = mockk<SimuleringFacade>()
        every { normalderService.nedreAlder(foedselsdato) } returns nedreAlder
        every { simuleringFacade.simulerAlderspensjon(any(), inkluderPensjonHvisUbetinget = true) } returns
                SimulertPensjonEllerAlternativ(
                    pensjon = null,
                    alternativ = SimulertAlternativ(
                        gradertUttakAlder = SimulertUttakAlder(Alder(63, 0), LocalDate.of(2026, 2, 1)),
                        uttakGrad = UttakGradKode.P_50,
                        heltUttakAlder = SimulertUttakAlder(Alder(67, 0), LocalDate.of(2030, 2, 1)),
                        resultStatus = SimulatorResultStatus.GOOD
                    )
                )

        val result = UttakService(simuleringFacade, normalderService) { LocalDate.of(2024, 1, 1) }
            .finnTidligstMuligUttak(simuleringSpec(foedselsdato = foedselsdato))

        result.feil shouldBe null
    }
})
