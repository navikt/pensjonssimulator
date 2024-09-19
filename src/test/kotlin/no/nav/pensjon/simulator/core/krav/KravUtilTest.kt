package no.nav.pensjon.simulator.core.krav

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.SimuleringSpec
import no.nav.pensjon.simulator.core.domain.Land
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
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
})

private fun simuleringSpecUtenUtlandPerioder(): SimuleringSpec =
    simuleringSpec(mutableListOf())

private fun simuleringSpecMedOverlappendeUtlandPerioder(): SimuleringSpec =
    simuleringSpec(
        // 2 delvis overlappende utenlandsperioder, hver med delvis overlapp med uttaksperiode;
        // da skal det lengste overlappet (4 måneder) velges:
        mutableListOf(
            UtlandPeriode(
                fom = LocalDate.of(2001, 1, 15), // overlapper uttaksperiode 2001...
                tom = LocalDate.of(2001, 9, 15), // ...1.aug.-15.sept. (1 hel måned)
                land = Land.AUS,
                arbeidet = false
            ),
            UtlandPeriode(
                fom = LocalDate.of(2001, 5, 15), // overlapper uttaksperiode 2001...
                tom = LocalDate.of(2001, 12, 15), // ...1.aug.-15.des. (4 hele måneder)
                land = Land.AUS,
                arbeidet = true
            )
        )
    )

private fun simuleringSpec(utlandPeriodeListe: MutableList<UtlandPeriode>) =
    SimuleringSpec(
        type = SimuleringType.ALDER,
        sivilstatus = SivilstatusType.UGIF,
        epsHarPensjon = false,
        foersteUttakDato = LocalDate.of(2001, 8, 1), // => uttaksperiode 2001 = 1.aug.-31.des.
        heltUttakDato = null,
        pid = null,
        avdoed = null,
        isTpOrigSimulering = false,
        simulerForTp = false,
        uttakGrad = UttakGradKode.P_100,
        forventetInntektBeloep = 0,
        inntektUnderGradertUttakBeloep = 0,
        inntektEtterHeltUttakBeloep = 0,
        inntektEtterHeltUttakAntallAar = 0,
        foedselAar = 0,
        boddUtenlands = false,
        utlandAntallAar = 0,
        utlandPeriodeListe = utlandPeriodeListe,
        fremtidigInntektListe = mutableListOf(),
        inntektOver1GAntallAar = 0,
        flyktning = null,
        epsHarInntektOver2G = false,
        rettTilOffentligAfpFom = null,
        erAnonym = false
    )
