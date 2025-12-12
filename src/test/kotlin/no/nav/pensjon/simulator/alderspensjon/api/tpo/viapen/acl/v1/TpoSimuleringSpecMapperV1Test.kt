package no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v1

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class TpoSimuleringSpecMapperV1Test : FunSpec({

    test("fromDto maps from DTO version 1 to domain - ikke-gradert uttak") {
        TpoSimuleringSpecMapperV1(personService = arrangePerson()).fromDto(
            source = TpoSimuleringSpecV1(
                pid = pid.value,
                sivilstatus = SivilstatusType.SAMB,
                epsPensjon = true,
                eps2G = false,
                utenlandsopphold = 5,
                simuleringType = SimuleringTypeEnum.ALDER,
                foersteUttakDato = LocalDate.of(1990, 1, 1),
                uttakGrad = UttakGradKode.P_100,
                heltUttakDato = null, //LocalDate.of(1990, 1, 1),
                antallArInntektEtterHeltUttak = 5,
                forventetInntekt = 123000,
                inntektUnderGradertUttak = null,
                inntektEtterHeltUttak = 80000
            )
        ) shouldBe SimuleringSpec(
            type = SimuleringTypeEnum.ALDER,
            sivilstatus = SivilstatusType.SAMB,
            epsHarPensjon = true,
            foersteUttakDato = LocalDate.of(1990, 1, 1),
            heltUttakDato = null,
            pid = pid,
            foedselDato = LocalDate.of(1963, 1, 15),
            avdoed = null,
            isTpOrigSimulering = true,
            simulerForTp = false,
            uttakGrad = UttakGradKode.P_100,
            forventetInntektBeloep = 123000,
            inntektUnderGradertUttakBeloep = 0,
            inntektEtterHeltUttakBeloep = 80000,
            inntektEtterHeltUttakAntallAar = 5,
            foedselAar = 0, // brukes ikke i denne kontekst
            utlandAntallAar = 5,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(), // empty in this context
            brukFremtidigInntekt = false,
            inntektOver1GAntallAar = 0,
            flyktning = null,
            epsHarInntektOver2G = false,
            livsvarigOffentligAfp = null,
            pre2025OffentligAfp = null,
            erAnonym = false,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = true,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true, // to produce månedsbeløp
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )
    }

    test("fromDto maps from DTO version 1 to domain - gradert uttak") {
        TpoSimuleringSpecMapperV1(personService = arrangePerson()).fromDto(
            source = TpoSimuleringSpecV1(
                pid = pid.value,
                sivilstatus = SivilstatusType.GIFT,
                epsPensjon = false,
                eps2G = true,
                utenlandsopphold = null,
                simuleringType = SimuleringTypeEnum.ENDR_ALDER,
                foersteUttakDato = LocalDate.of(2030, 1, 1),
                uttakGrad = UttakGradKode.P_40,
                heltUttakDato = LocalDate.of(2034, 6, 1),
                antallArInntektEtterHeltUttak = 5,
                forventetInntekt = 123000,
                inntektUnderGradertUttak = 99000,
                inntektEtterHeltUttak = 80000
            )
        ) shouldBe SimuleringSpec(
            type = SimuleringTypeEnum.ENDR_ALDER,
            sivilstatus = SivilstatusType.GIFT,
            epsHarPensjon = false,
            foersteUttakDato = LocalDate.of(2030, 1, 1),
            heltUttakDato = LocalDate.of(2034, 6, 1),
            pid = pid,
            foedselDato = LocalDate.of(1963, 1, 15),
            avdoed = null,
            isTpOrigSimulering = true,
            simulerForTp = false,
            uttakGrad = UttakGradKode.P_40,
            forventetInntektBeloep = 123000,
            inntektUnderGradertUttakBeloep = 99000,
            inntektEtterHeltUttakBeloep = 80000,
            inntektEtterHeltUttakAntallAar = 5,
            foedselAar = 0, // brukes ikke i denne kontekst
            utlandAntallAar = 0,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(), // empty in this context
            brukFremtidigInntekt = false,
            inntektOver1GAntallAar = 0,
            flyktning = null,
            epsHarInntektOver2G = true,
            livsvarigOffentligAfp = null,
            pre2025OffentligAfp = null,
            erAnonym = false,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = true,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true, // to produce månedsbeløp
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )
    }
})

private fun arrangePerson(): GeneralPersonService =
    mockk<GeneralPersonService>().apply {
        every { foedselsdato(pid) } returns LocalDate.of(1963, 1, 15)
    }
