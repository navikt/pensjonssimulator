package no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.OffentligTjenestepensjonFra2025SimuleringSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonInntektSpec
import java.time.LocalDate

class OffentligTjenestepensjonFra2025SimuleringSpecMapperV1Test : ShouldSpec({

    should("map values from DTO to domain object") {
        OffentligTjenestepensjonFra2025SimuleringSpecMapperV1.fromDto(
            SimulerOffentligTjenestepensjonFra2025SpecV1(
                pid.value,
                foedselsdato = LocalDate.of(1965, 1, 15),
                uttaksdato = LocalDate.of(2030, 1, 1),
                sisteInntekt = 1,
                aarIUtlandetEtter16 = 2,
                brukerBaOmAfp = true,
                epsPensjon = false,
                eps2G = true,
                fremtidigeInntekter = listOf(
                    SimulerTjenestepensjonFremtidigInntektDto(
                        fraOgMed = LocalDate.of(2029, 2, 1),
                        aarligInntekt = 3
                    )
                ),
                erApoteker = false
            )
        ) shouldBe OffentligTjenestepensjonFra2025SimuleringSpec(
            pid,
            foedselsdato = LocalDate.of(1965, 1, 15),
            uttaksdato = LocalDate.of(2030, 1, 1),
            sisteInntekt = 1,
            utlandAntallAar = 2,
            afpErForespurt = true,
            epsHarPensjon = false,
            epsHarInntektOver2G = true,
            fremtidigeInntekter = listOf(
                TjenestepensjonInntektSpec(
                    fom = LocalDate.of(2029, 2, 1),
                    aarligInntekt = 3
                )
            ),
            gjelderApoteker = false
        )
    }
})
