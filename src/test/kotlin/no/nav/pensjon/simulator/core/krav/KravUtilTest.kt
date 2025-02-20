package no.nav.pensjon.simulator.core.krav

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.trygd.UtlandPeriode
import java.time.LocalDate

class KravUtilTest : FunSpec({

    test("utlandMaanederInnenforAaret returnerer 0 hvis ingen utenlandsperioder") {
        KravUtil.utlandMaanederInnenforAaret(
            spec = simuleringSpecUtenUtlandPerioder(), year = 2020
        ) shouldBe 0
    }

    test("utlandMaanederInnenforRestenAvAaret bruker utenlandsperioden med mest overlapp") {
        KravUtil.utlandMaanederInnenforRestenAvAaret(
            simuleringSpecMedOverlappendeUtlandPerioder()
        ) shouldBe 4
    }

    test("utlandMaanederInnenforRestenAvAaret returnerer 0 hvis ingen utenlandsperioder") {
        KravUtil.utlandMaanederInnenforRestenAvAaret(
            simuleringSpecUtenUtlandPerioder()
        ) shouldBe 0
    }

    test("utlandMaanederFraAarStartTilFoersteUttakDato bruker utenlandsperioden med mest overlapp") {
        KravUtil.utlandMaanederFraAarStartTilFoersteUttakDato(
            simuleringSpecMedOverlappendeUtlandPerioder()
        ) shouldBe 6
    }

    test("utlandMaanederFraAarStartTilFoersteUttakDato returnerer 0 hvis ingen utenlandsperioder") {
        KravUtil.utlandMaanederFraAarStartTilFoersteUttakDato(
            simuleringSpecUtenUtlandPerioder()
        ) shouldBe 0
    }
})

private fun simuleringSpecUtenUtlandPerioder(): SimuleringSpec =
    simuleringSpec(mutableListOf())

// 2 delvis overlappende utenlandsperioder, hver med delvis overlapp med uttaksperiode;
// da skal det lengste overlappet (6 måneder) velges:
// inntektsperiode før uttak =  1.jan.-31.juli
// utland-periode 1          = 15.jan.-15.sept. => overlapp 15.jan.-31.juli = 6 måneder
// utland-periode 2          = 15.mai -15.des.  => overlapp 15.mai.-31.juli = 2 måneder
private fun simuleringSpecMedOverlappendeUtlandPerioder(): SimuleringSpec =
    simuleringSpec(
        // 2 delvis overlappende utenlandsperioder, hver med delvis overlapp med uttaksperiode;
        // da skal det lengste overlappet (4 måneder) velges:
        mutableListOf(
            UtlandPeriode(
                fom = LocalDate.of(2001, 1, 15),
                tom = LocalDate.of(2001, 9, 15),
                land = LandkodeEnum.AUS,
                arbeidet = false
            ),
            UtlandPeriode(
                fom = LocalDate.of(2001, 5, 15),
                tom = LocalDate.of(2001, 12, 15),
                land = LandkodeEnum.AUS,
                arbeidet = true
            )
        )
    )

private fun simuleringSpec(utlandPeriodeListe: MutableList<UtlandPeriode>) =
    SimuleringSpec(
        type = SimuleringType.ALDER,
        sivilstatus = SivilstatusType.UGIF,
        epsHarPensjon = false,
        foersteUttakDato = LocalDate.of(2001, 8, 1), // => uttaksperiode 2001 = 1.aug.-31.des., inntektsperiode før uttak = 1.jan.-31.juli
        heltUttakDato = null,
        pid = null,
        foedselDato = null,
        avdoed = null,
        isTpOrigSimulering = false,
        simulerForTp = false,
        uttakGrad = UttakGradKode.P_100,
        forventetInntektBeloep = 0,
        inntektUnderGradertUttakBeloep = 0,
        inntektEtterHeltUttakBeloep = 0,
        inntektEtterHeltUttakAntallAar = 0,
        foedselAar = 0,
        utlandAntallAar = 0,
        utlandPeriodeListe = utlandPeriodeListe,
        fremtidigInntektListe = mutableListOf(),
        inntektOver1GAntallAar = 0,
        flyktning = null,
        epsHarInntektOver2G = false,
        rettTilOffentligAfpFom = null,
        erAnonym = false,
        ignoreAvslag = false,
        afpOrdning = null,
        afpInntektMaanedFoerUttak = null,
        isHentPensjonsbeholdninger = false,
        isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
        onlyVilkaarsproeving = false,
        epsKanOverskrives = false
    )
