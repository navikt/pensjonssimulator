package no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.spec

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.trygd.UtlandPeriode
import no.nav.pensjon.simulator.inntekt.InntektService
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate
import java.util.*

class NavSimuleringSpecMapperV3Test : FunSpec({

    test("fromNavSimuleringSpecV3 should fetch foedselsdato and map values") {
        NavSimuleringSpecMapperV3(
            personService = Arrange.foedselsdato(1963, 4, 5),
            inntektService = arrangeGrunnbeloep()
        ).fromNavSimuleringSpecV3(
            source = NavSimuleringSpecV3(
                pid.value,
                sivilstand = NavSivilstandSpecV3.GJPA,
                uttaksar = 1,
                sisteInntekt = 2000,
                simuleringstype = NavSimuleringTypeSpecV3.ENDR_ALDER,
                gradertUttak = NavSimuleringGradertUttakSpecV3(
                    grad = UttakGradKode.P_20,
                    uttakFomAlder = NavSimuleringAlderSpecV3(aar = 62, maaneder = 3),
                    aarligInntekt = 4000
                ),
                heltUttak = NavSimuleringHeltUttakSpecV3(
                    uttakFomAlder = NavSimuleringAlderSpecV3(aar = 66, maaneder = 7),
                    aarligInntekt = 5000,
                    inntektTomAlder = NavSimuleringAlderSpecV3(aar = 74, maaneder = 5)
                ),
                aarUtenlandsEtter16Aar = 8, // ignoreres; i denne kontekst brukes utenlandsperiodeListe
                epsHarPensjon = true,
                epsHarInntektOver2G = false,
                fremtidigInntektListe = listOf(
                    NavSimuleringInntektSpecV3(
                        aarligInntekt = 6000,
                        fom = dateAtNoon(2021, Calendar.JANUARY, 1)
                    )
                ),
                utenlandsperiodeListe = listOf(
                    NavSimuleringUtlandSpecV3(
                        fom = dateAtNoon(2021, Calendar.JANUARY, 1),
                        tom = dateAtNoon(2022, Calendar.DECEMBER, 19),
                        land = "ALB",
                        arbeidetUtenlands = true
                    )
                )
            ),
        ) shouldBe SimuleringSpec(
            type = SimuleringTypeEnum.ENDR_ALDER,
            sivilstatus = SivilstatusType.GJPA,
            epsHarPensjon = true,
            foersteUttakDato = LocalDate.of(2025, 8, 1),
            heltUttakDato = LocalDate.of(2029, 12, 1),
            pid = pid,
            foedselDato = LocalDate.of(1963, 4, 5),
            avdoed = null,
            isTpOrigSimulering = false,
            simulerForTp = false,
            uttakGrad = UttakGradKode.P_20,
            forventetInntektBeloep = 2000,
            inntektUnderGradertUttakBeloep = 4000,
            inntektEtterHeltUttakBeloep = 5000,
            inntektEtterHeltUttakAntallAar = 9,
            foedselAar = 0, // brukes ikke i denne kontekst
            utlandAntallAar = 0, // siden utlandPeriodeListe brukes isteden
            utlandPeriodeListe = mutableListOf(
                UtlandPeriode(
                    fom = LocalDate.of(2021, 1, 1),
                    tom = LocalDate.of(2022, 12, 19),
                    land = LandkodeEnum.ALB,
                    arbeidet = true
                )
            ),
            fremtidigInntektListe = mutableListOf(), // empty in this context
            brukFremtidigInntekt = false,
            inntektOver1GAntallAar = 0,
            flyktning = false,
            epsHarInntektOver2G = false,
            rettTilOffentligAfpFom = null,
            pre2025OffentligAfp = null,
            erAnonym = false,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = false,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true, // to produce månedsbeløp
            onlyVilkaarsproeving = false,
            epsKanOverskrives = true
        )
    }

    test("fromNavSimuleringSpecV3 should use heltUttakDato = null if ugradert uttak") {
        NavSimuleringSpecMapperV3(
            personService = Arrange.foedselsdato(1963, 4, 5),
            inntektService = arrangeGrunnbeloep()
        ).fromNavSimuleringSpecV3(
            source = NavSimuleringSpecV3(
                pid.value,
                sivilstand = NavSivilstandSpecV3.ENKE,
                uttaksar = 0,
                sisteInntekt = 2000,
                simuleringstype = NavSimuleringTypeSpecV3.ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG,
                gradertUttak = null, // => ugradert uttak
                heltUttak = NavSimuleringHeltUttakSpecV3(
                    uttakFomAlder = NavSimuleringAlderSpecV3(aar = 66, maaneder = 7),
                    aarligInntekt = 5000,
                    inntektTomAlder = NavSimuleringAlderSpecV3(aar = 73, maaneder = 0)
                ),
                aarUtenlandsEtter16Aar = 0,
                epsHarPensjon = false,
                epsHarInntektOver2G = true,
                fremtidigInntektListe = emptyList(),
                utenlandsperiodeListe = emptyList()
            ),
        ) shouldBe SimuleringSpec(
            type = SimuleringTypeEnum.ENDR_AP_M_AFP_OFFENTLIG_LIVSVARIG,
            sivilstatus = SivilstatusType.ENKE,
            epsHarPensjon = false,
            foersteUttakDato = LocalDate.of(2029, 12, 1), // dato for helt uttak
            heltUttakDato = null, // siden ugradert uttak
            pid = pid,
            foedselDato = LocalDate.of(1963, 4, 5),
            avdoed = null,
            isTpOrigSimulering = false,
            simulerForTp = false,
            uttakGrad = UttakGradKode.P_100,
            forventetInntektBeloep = 2000,
            inntektUnderGradertUttakBeloep = 0,
            inntektEtterHeltUttakBeloep = 5000,
            inntektEtterHeltUttakAntallAar = 8,
            foedselAar = 0,
            utlandAntallAar = 0,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(),
            brukFremtidigInntekt = false,
            inntektOver1GAntallAar = 0,
            flyktning = false,
            epsHarInntektOver2G = true,
            rettTilOffentligAfpFom = null,
            pre2025OffentligAfp = null,
            erAnonym = false,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = false,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true,
            onlyVilkaarsproeving = false,
            epsKanOverskrives = true
        )
    }
})

private fun arrangeGrunnbeloep(): InntektService =
    mockk<InntektService>().apply {
        every { hentSisteMaanedsInntektOver1G(false) } returns 100000
    }
