package no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.alderspensjon.spec.SimuleringstypeDeducer
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class AlderspensjonSpecMapperV3Test : ShouldSpec({

    should("map DTO med privat AFP, ikke gradert") {
        AlderspensjonSpecMapperV3(
            personService = Arrange.foedselsdato(1963, 1, 15),
            simuleringstypeDeducer = arrangeSimuleringstype()
        ).fromDtoV3(
            AlderspensjonSpecV3(
                fnr = pid.value,
                sivilstandVedPensjonering = SivilstatusSpecV3.GJPA,
                forsteUttak = UttaksperiodeSpecV3(
                    datoFom = LocalDate.of(2030, 1, 1).toNorwegianDateAtNoon(),
                    grad = 100
                ),
                heltUttak = null,
                arIUtlandetEtter16 = 1,
                epsPensjon = true,
                eps2G = false,
                fremtidigInntektListe = listOf(
                    InntektSpecV3(
                        arligInntekt = 1000,
                        fomDato = LocalDate.of(2031, 2, 1).toNorwegianDateAtNoon()
                    )
                ),
                simulerMedAfpPrivat = true
            )
        ) shouldBe SimuleringSpec(
            SimuleringTypeEnum.ALDER_M_AFP_PRIVAT,
            SivilstatusType.GJPA,
            epsHarPensjon = true,
            foersteUttakDato = LocalDate.of(2030, 1, 1),
            heltUttakDato = null,
            pid = pid,
            foedselDato = LocalDate.of(1963, 1, 15),
            avdoed = null,
            isTpOrigSimulering = true,
            simulerForTp = false,
            uttakGrad = UttakGradKode.P_100,
            forventetInntektBeloep = 0,
            inntektUnderGradertUttakBeloep = 0,
            inntektEtterHeltUttakBeloep = 0,
            inntektEtterHeltUttakAntallAar = null,
            foedselAar = 0,
            utlandAntallAar = 1,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(
                FremtidigInntekt(
                    aarligInntektBeloep = 1000,
                    fom = LocalDate.of(2031, 2, 1)
                )
            ),
            brukFremtidigInntekt = true,
            inntektOver1GAntallAar = 0,
            flyktning = null,
            epsHarInntektOver2G = false,
            rettTilOffentligAfpFom = null,
            pre2025OffentligAfp = null,
            erAnonym = false,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = true,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true,
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )
    }
})

private fun arrangeSimuleringstype(): SimuleringstypeDeducer = mockk<SimuleringstypeDeducer> {
    every {
        deduceSimuleringstype(any(), any(), any())
    } returns SimuleringTypeEnum.ALDER_M_AFP_PRIVAT
}
