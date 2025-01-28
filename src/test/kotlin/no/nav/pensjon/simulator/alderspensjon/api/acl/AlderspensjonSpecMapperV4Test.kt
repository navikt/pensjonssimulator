package no.nav.pensjon.simulator.alderspensjon.api.acl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alderspensjon.api.tpo.direct.acl.v4.AlderspensjonSpecMapperV4
import no.nav.pensjon.simulator.alderspensjon.api.tpo.direct.acl.v4.AlderspensjonSpecV4
import no.nav.pensjon.simulator.alderspensjon.api.tpo.direct.acl.v4.PensjonInntektSpecV4
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class AlderspensjonSpecMapperV4Test : FunSpec({

    test("fromSpecV4 maps from DTO version 4 to domain") {
        AlderspensjonSpecMapperV4.fromSpecV4(
            source = AlderspensjonSpecV4(
                personId = pid.value,
                gradertUttak = null,
                heltUttakFraOgMedDato = "2031-02-03",
                epsPensjon = true,
                eps2G = false,
                aarIUtlandetEtter16 = 5,
                fremtidigInntektListe = listOf(
                    PensjonInntektSpecV4(
                        aarligInntekt = 123000,
                        fraOgMedDato = "2029-05-06"
                    )
                ),
                rettTilAfpOffentligDato = "2032-03-04"
            ),
            foedselsdato = LocalDate.of(1964, 1, 1)
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
                    isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true
                )
    }
})
