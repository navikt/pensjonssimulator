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
    val foedselsdato = LocalDate.now().minusYears(62).minusMonths(1)

    test("fromNavSimuleringSpecV3 should fetch fødselsdato and map values") {
        val fremtidigInntektListeFraAar = foedselsdato.plusYears(58).year
        val utenlandsperiodeStartAar = foedselsdato.plusYears(58).year
        val foersteUttaksdato = foedselsdato.plusYears(62).plusMonths(4).withDayOfMonth(1)
        val heltUttaksdato = foedselsdato.plusYears(66).plusMonths(8).withDayOfMonth(1)
        NavSimuleringSpecMapperV3(
            personService = Arrange.foedselsdato(foedselsdato.year, foedselsdato.monthValue, foedselsdato.dayOfMonth),
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
                        fom = dateAtNoon(fremtidigInntektListeFraAar, Calendar.JANUARY, 1)
                    )
                ),
                utenlandsperiodeListe = listOf(
                    NavSimuleringUtlandSpecV3(
                        fom = dateAtNoon(utenlandsperiodeStartAar, Calendar.JANUARY, 1),
                        tom = dateAtNoon(utenlandsperiodeStartAar + 1, Calendar.DECEMBER, 19),
                        land = "ALB",
                        arbeidetUtenlands = true
                    )
                )
            ),
        ) shouldBe SimuleringSpec(
            type = SimuleringTypeEnum.ENDR_ALDER,
            sivilstatus = SivilstatusType.GJPA,
            epsHarPensjon = true,
            foersteUttakDato = foersteUttaksdato,
            heltUttakDato = heltUttaksdato,
            pid = pid,
            foedselDato = foedselsdato,
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
                    fom = LocalDate.of(utenlandsperiodeStartAar, 1, 1),
                    tom = LocalDate.of(utenlandsperiodeStartAar + 1, 12, 19),
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
            epsKanOverskrives = false
        )
    }

    test("fromNavSimuleringSpecV3 should use heltUttakDato = null if ugradert uttak") {
        val foersteUttaksdato = foedselsdato.plusYears(66).plusMonths(8).withDayOfMonth(1)
        NavSimuleringSpecMapperV3(
            personService = Arrange.foedselsdato(foedselsdato.year, foedselsdato.monthValue, foedselsdato.dayOfMonth),
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
            foersteUttakDato = foersteUttaksdato, // dato for helt uttak
            heltUttakDato = null, // siden ugradert uttak
            pid = pid,
            foedselDato = foedselsdato,
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
            epsKanOverskrives = false
        )
    }
})

private fun arrangeGrunnbeloep(): InntektService =
    mockk<InntektService>().apply {
        every { hentSisteMaanedsInntektOver1G(false) } returns 100000
    }
