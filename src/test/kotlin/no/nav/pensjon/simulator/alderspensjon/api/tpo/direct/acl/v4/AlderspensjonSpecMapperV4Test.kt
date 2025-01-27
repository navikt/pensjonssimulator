package no.nav.pensjon.simulator.alderspensjon.api.tpo.direct.acl.v4

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class AlderspensjonSpecMapperV4Test : FunSpec({

    test("fromSpecV4 maps relevant values") {
        AlderspensjonSpecMapperV4.fromSpecV4(
            AlderspensjonSpecV4(
                personId = pid.value,
                gradertUttak = GradertUttakSpecV4(
                    fraOgMedDato = "2028-11-01",
                    uttaksgrad = 20
                ),
                heltUttakFraOgMedDato = "2030-01-01",
                aarIUtlandetEtter16 = 3,
                epsPensjon = true,
                eps2G = false,
                fremtidigInntektListe = listOf(
                    PensjonInntektSpecV4(
                        aarligInntekt = 123000,
                        fraOgMedDato = "2029-02-01"
                    )
                ),
                rettTilAfpOffentligDato = "2027-08-01"
            ), LocalDate.of(1964, 5, 6)
        ) shouldBe SimuleringSpec(
            type = SimuleringType.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG,
            sivilstatus = SivilstatusType.GIFT,
            epsHarPensjon = true,
            foersteUttakDato = LocalDate.of(2028, 11, 1),
            heltUttakDato = LocalDate.of(2030, 1, 1),
            pid = pid,
            foedselDato = LocalDate.of(1964, 5, 6),
            avdoed = null,
            isTpOrigSimulering = true,
            simulerForTp = false,
            uttakGrad = UttakGradKode.P_20,
            forventetInntektBeloep = 0,
            inntektUnderGradertUttakBeloep = 0,
            inntektEtterHeltUttakBeloep = 0,
            inntektEtterHeltUttakAntallAar = null,
            foedselAar = 1964,
            boddUtenlands = false,
            utlandAntallAar = 3,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(
                FremtidigInntekt(
                    aarligInntektBeloep = 123000,
                    fom = LocalDate.of(2029, 2, 1)
                )
            ),
            inntektOver1GAntallAar = 0,
            flyktning = false,
            epsHarInntektOver2G = false,
            rettTilOffentligAfpFom = LocalDate.of(2027, 8, 1),
            afpOrdning = null,
            afpInntektMaanedFoerUttak = null,
            erAnonym = false,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = true,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true
        )
    }

    test("fromSpecV4 throws 'Bad Request' exception if gradertUttak lacks fraOgMedDato") {
        val exception =
            shouldThrow<BadRequestException> {
                AlderspensjonSpecMapperV4.fromSpecV4(
                    AlderspensjonSpecV4(
                        personId = pid.value,
                        gradertUttak = GradertUttakSpecV4(
                            fraOgMedDato = null,
                            uttaksgrad = 20
                        ),
                        heltUttakFraOgMedDato = "2030-01-01",
                        aarIUtlandetEtter16 = 0,
                        epsPensjon = false,
                        eps2G = false,
                        fremtidigInntektListe = emptyList(),
                        rettTilAfpOffentligDato = null
                    ), LocalDate.of(1964, 5, 6)
                )
            }

        exception.message shouldBe "gradertUttak.fraOgMedDato missing"
    }
})
