package no.nav.pensjon.simulator.alderspensjon.api.tpo.direct.acl.v4

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alderspensjon.spec.AlderspensjonSpec
import no.nav.pensjon.simulator.alderspensjon.spec.GradertUttakSpec
import no.nav.pensjon.simulator.alderspensjon.spec.PensjonInntektSpec
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class AlderspensjonSpecMapperV4Test : FunSpec({

    test("fromDto maps from DTO version 4 to domain - helt uttak") {
        AlderspensjonSpecMapperV4.fromDto(
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
            )
        ) shouldBe
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
                    livsvarigOffentligAfpRettFom = LocalDate.of(2032, 3, 4)
                )
    }

    test("fromDto maps from DTO version 4 to domain - gradertUttak, 2 inntekter") {
        AlderspensjonSpecMapperV4.fromDto(
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
                    ), PensjonInntektSpecV4(
                        aarligInntekt = 234000,
                        fraOgMedDato = "2030-03-01"
                    )
                ),
                rettTilAfpOffentligDato = null
            )
        ) shouldBe AlderspensjonSpec(
            pid,
            gradertUttak = GradertUttakSpec(
                uttaksgrad = UttakGradKode.P_20,
                fom = LocalDate.of(2028, 11, 1),
            ),
            heltUttakFom = LocalDate.of(2030, 1, 1),
            antallAarUtenlandsEtter16 = 3,
            epsHarPensjon = true,
            epsHarInntektOver2G = false,
            fremtidigInntektListe = listOf(
                PensjonInntektSpec(
                    aarligBeloep = 123000,
                    fom = LocalDate.of(2029, 2, 1)
                ), PensjonInntektSpec(
                    aarligBeloep = 234000,
                    fom = LocalDate.of(2030, 3, 1)
                )
            ),
            livsvarigOffentligAfpRettFom = null
        )
    }

    test("fromDto throws 'Bad Request' exception if gradertUttak lacks fraOgMedDato") {
        val exception =
            shouldThrow<BadRequestException> {
                AlderspensjonSpecMapperV4.fromDto(
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
                    )
                )
            }

        exception.message shouldBe "gradertUttak.fraOgMedDato missing"
    }
})
