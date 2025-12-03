package no.nav.pensjon.simulator.hybrid.api.samhandler.acl.v3

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.alderspensjon.spec.SimuleringstypeDeducer
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class AlderspensjonOgPrivatAfpSpecMapperV3Test : FunSpec({

    test("'fromDto' med privat AFP, ikke gradert") {
        AlderspensjonOgPrivatAfpSpecMapperV3(
            personService = Arrange.foedselsdato(1963, 1, 15),
            simuleringstypeDeducer = arrangeSimuleringstype()
        ).fromDto(
            AlderspensjonOgPrivatAfpSpecV3(
                personident = pid.value,
                aarligInntektFoerUttak = 2000,
                antallInntektsaarEtterHeltUttak = 2,
                foersteUttak = ApOgPrivatAfpUttakSpecV3(
                    fomDato = LocalDate.of(2030, 1, 1),
                    grad = 100,
                    aarligInntekt = 1000
                ),
                heltUttak = null,
                aarIUtlandetEtter16 = 1,
                sivilstatusVedPensjonering = ApOgPrivatAfpSivilstatusSpecV3.GJES,
                harEpsPensjon = true,
                harEpsPensjonsgivendeInntektOver2G = false,
                simulerPrivatAfp = true
            )
        ) shouldBe SimuleringSpec(
            SimuleringTypeEnum.ALDER_M_AFP_PRIVAT,
            SivilstatusType.GJES,
            epsHarPensjon = true,
            foersteUttakDato = LocalDate.of(2030, 1, 1),
            heltUttakDato = null,
            pid = pid,
            foedselDato = LocalDate.of(1963, 1, 15),
            avdoed = null,
            isTpOrigSimulering = true,
            simulerForTp = false,
            uttakGrad = UttakGradKode.P_100,
            forventetInntektBeloep = 2000,
            inntektUnderGradertUttakBeloep = 0,
            inntektEtterHeltUttakBeloep = 1000,
            inntektEtterHeltUttakAntallAar = 2,
            foedselAar = 0,
            utlandAntallAar = 1,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(),
            brukFremtidigInntekt = true,
            inntektOver1GAntallAar = 0,
            flyktning = null,
            epsHarInntektOver2G = false,
            livsvarigOffentligAfp = null,
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
