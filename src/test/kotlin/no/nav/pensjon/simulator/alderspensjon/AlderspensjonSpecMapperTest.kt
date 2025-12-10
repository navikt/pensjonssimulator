package no.nav.pensjon.simulator.alderspensjon

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alderspensjon.spec.AlderspensjonSpec
import no.nav.pensjon.simulator.alderspensjon.spec.PensjonInntektSpec
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class AlderspensjonSpecMapperTest : ShouldSpec({

    should("map from particular to general specification - 1. gangsuttak + offentlig AFP") {
        AlderspensjonSpecMapper.simuleringSpec(
            source = alderspensjonSpec(livsvarigOffentligAfpRettFom = LocalDate.of(2032, 3, 4)),
            foedselsdato = LocalDate.of(1964, 1, 1),
            simuleringstypeDeducer = Arrange.simuleringstype(
                type = SimuleringTypeEnum.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG,
                uttakFom = LocalDate.of(2031, 2, 3),
                livsvarigOffentligAfpRettFom = LocalDate.of(2032, 3, 4)
            )
        ) shouldBe
                simuleringSpec(
                    type = SimuleringTypeEnum.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG,
                    livsvarigOffentligAfpRettFom = LocalDate.of(2032, 3, 4)
                )
    }

    should("map from particular to general specification - endring av pensjon uten AFP") {
        AlderspensjonSpecMapper.simuleringSpec(
            source = alderspensjonSpec(livsvarigOffentligAfpRettFom = null), // => uten AFP
            foedselsdato = LocalDate.of(1964, 1, 1),
            simuleringstypeDeducer = Arrange.simuleringstype(
                type = SimuleringTypeEnum.ENDR_ALDER,
                uttakFom = LocalDate.of(2031, 2, 3),
                livsvarigOffentligAfpRettFom = null
            )
        ) shouldBe
                simuleringSpec(type = SimuleringTypeEnum.ENDR_ALDER, livsvarigOffentligAfpRettFom = null)
    }
})

private fun alderspensjonSpec(livsvarigOffentligAfpRettFom: LocalDate?) =
    AlderspensjonSpec(
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
        livsvarigOffentligAfpRettFom
    )

private fun simuleringSpec(type: SimuleringTypeEnum, livsvarigOffentligAfpRettFom: LocalDate?) =
    SimuleringSpec(
        type,
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
        brukFremtidigInntekt = true,
        inntektOver1GAntallAar = 0,
        flyktning = false,
        epsHarInntektOver2G = false,
        livsvarigOffentligAfp = livsvarigOffentligAfpRettFom?.let { LivsvarigOffentligAfpSpec(rettTilAfpFom = it) },
        pre2025OffentligAfp = null,
        erAnonym = false,
        ignoreAvslag = false,
        isHentPensjonsbeholdninger = true,
        isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true,
        onlyVilkaarsproeving = false,
        epsKanOverskrives = false
    )
