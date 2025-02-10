package no.nav.pensjon.simulator.alderspensjon

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alderspensjon.spec.AlderspensjonSpec
import no.nav.pensjon.simulator.alderspensjon.spec.PensjonInntektSpec
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class AlderspensjonSpecMapperTest : FunSpec({

    test("simuleringSpec maps from particular specification to general specification") {
        AlderspensjonSpecMapper.simuleringSpec(
            source = AlderspensjonSpec(
                pid,
                gradertUttak = null,
                heltUttakFom = LocalDate.of(2031, 2, 3),
                antallAarUtenlandsEtter16 = 5,
                epsHarPensjon = true,
                epsHarInntektOver2G = false,
                fremtidigInntektListe = listOf(
                    PensjonInntektSpec(
                        aarligBeloep = 123000,
                        fom = LocalDate.of(2029, 5, 6)
                    )
                ),
                livsvarigOffentligAfpRettFom = LocalDate.of(2032, 3, 4)
            ),
            foedselsdato = LocalDate.of(1964, 1, 1),
            erFoerstegangsuttak = false
        ) shouldBe
                SimuleringSpec(
                    type = SimuleringType.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG,
                    sivilstatus = SivilstatusType.GIFT,
                    epsHarPensjon = true,
                    foersteUttakDato = LocalDate.of(2031, 2, 3),
                    heltUttakDato = null,
                    pid = pid,
                    foedselDato = LocalDate.of(1964, 1, 1),
                    avdoed = null,
                    isTpOrigSimulering = true,
                    simulerForTp = false,
                    uttakGrad = UttakGradKode.P_100,
                    forventetInntektBeloep = 0,
                    inntektUnderGradertUttakBeloep = 0,
                    inntektEtterHeltUttakBeloep = 0,
                    inntektEtterHeltUttakAntallAar = null,
                    foedselAar = 1964,
                    utlandAntallAar = 5,
                    utlandPeriodeListe = mutableListOf(),
                    fremtidigInntektListe = mutableListOf(
                        FremtidigInntekt(
                            aarligInntektBeloep = 123000,
                            fom = LocalDate.of(2029, 5, 6)
                        )
                    ),
                    inntektOver1GAntallAar = 0,
                    flyktning = false,
                    epsHarInntektOver2G = false,
                    rettTilOffentligAfpFom = LocalDate.of(2032, 3, 4),
                    afpOrdning = null,
                    afpInntektMaanedFoerUttak = null,
                    erAnonym = false,
                    ignoreAvslag = false,
                    isHentPensjonsbeholdninger = true,
                    isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true,
                    onlyVilkaarsproeving = false
                )
    }
})
