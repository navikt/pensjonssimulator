package no.nav.pensjon.simulator.alderspensjon.api.nav.direct.anonym.acl.v1.spec

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import java.time.LocalDate

class AnonymSimuleringSpecMapperV1Test : FunSpec({

    test("fromAnonymSimuleringSpecV1 should map values from DTO to spec and use January 1 as fødselsdato") {
        AnonymSimuleringSpecMapperV1.fromAnonymSimuleringSpecV1(
            AnonymSimuleringSpecV1(
                simuleringType = "ALDER",
                fodselsar = 1963,
                forventetInntekt = 1,
                antArInntektOverG = 2,
                forsteUttakDato = LocalDate.of(2021, 2, 3),
                utg = "P_50",
                inntektUnderGradertUttak = 6,
                heltUttakDato = LocalDate.of(2024, 5, 6),
                inntektEtterHeltUttak = 7,
                antallArInntektEtterHeltUttak = 8,
                utenlandsopphold = 9,
                sivilstatus = "GIFT",
                epsPensjon = false,
                eps2G = true
            )
        ) shouldBe SimuleringSpec(
            type = SimuleringTypeEnum.ALDER,
            foedselAar = 1963,
            foedselDato = LocalDate.of(1963, 1, 1), // January 1
            foersteUttakDato = LocalDate.of(2021, 2, 3),
            heltUttakDato = LocalDate.of(2024, 5, 6),
            uttakGrad = UttakGradKode.P_50,
            forventetInntektBeloep = 1,
            inntektOver1GAntallAar = 2,
            inntektUnderGradertUttakBeloep = 6,
            inntektEtterHeltUttakBeloep = 7,
            inntektEtterHeltUttakAntallAar = 8,
            utlandAntallAar = 9,
            sivilstatus = SivilstatusType.GIFT,
            epsHarPensjon = false,
            epsHarInntektOver2G = true,
            pid = null,
            avdoed = null,
            isTpOrigSimulering = false,
            simulerForTp = false,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(),
            brukFremtidigInntekt = false,
            flyktning = false,
            rettTilOffentligAfpFom = null,
            pre2025OffentligAfp = null,
            erAnonym = true,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = false,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )
    }
})
