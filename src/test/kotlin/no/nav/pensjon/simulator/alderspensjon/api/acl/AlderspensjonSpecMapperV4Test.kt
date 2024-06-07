package no.nav.pensjon.simulator.alderspensjon.api.acl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alderspensjon.AlderspensjonSpec
import no.nav.pensjon.simulator.alderspensjon.InntektSpec
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

class AlderspensjonSpecMapperV4Test : FunSpec({

    test("fromSpecV4 maps from DTO version 4 to domain") {
        AlderspensjonSpecMapperV4.fromSpecV4(
            AlderspensjonSpecV4(
                personId = "00000011111",
                gradertUttak = null,
                heltUttakFraOgMedDato = "2031-02-03",
                epsPensjon = true,
                eps2G = false,
                arIUtlandetEtter16 = 5,
                fremtidigInntektListe = listOf(
                    PensjonInntektSpecV4(
                        arligInntekt = 123000,
                        fraOgMedDato = "2029-05-06"
                    )
                ),
                rettTilAfpOffentligDato = "2032-03-04"
            )
        ) shouldBe
                AlderspensjonSpec(
                    pid = Pid("00000011111"),
                    gradertUttak = null,
                    heltUttakFom = LocalDate.of(2031, 2, 3),
                    antallAarUtenlandsEtter16Aar = 5,
                    epsHarPensjon = true,
                    epsHarInntektOver2G = false,
                    fremtidigInntektListe = listOf(
                        InntektSpec(
                            aarligBeloep = 123000,
                            fom = LocalDate.of(2029, 5, 6)
                        )
                    ),
                    rettTilAfpOffentligDato = LocalDate.of(2032, 3, 4)
                )
    }
})
