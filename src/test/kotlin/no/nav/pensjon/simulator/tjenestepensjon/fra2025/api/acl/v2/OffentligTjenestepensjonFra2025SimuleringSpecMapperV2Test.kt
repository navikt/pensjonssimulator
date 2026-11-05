package no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v2

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.OffentligTjenestepensjonFra2025SimuleringSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonInntektSpec
import java.time.LocalDate

class OffentligTjenestepensjonFra2025SimuleringSpecMapperV2Test : ShouldSpec({

    listOf(true, false).forEach { flagg ->
        should("map values from DTO to domain object with brukerBaOmAfp=$flagg") {
            val source = SimulerOffentligTjenestepensjonFra2025SpecV2(
                pid = pid.value,
                foedselsdato = LocalDate.of(1965, 1, 15),
                uttaksdato = LocalDate.of(2030, 1, 1),
                sisteInntekt = 500000,
                aarIUtlandetEtter16 = 2,
                brukerBaOmAfp = flagg,
                epsPensjon = !flagg,
                eps2G = flagg,
                fremtidigeInntekter = listOf(
                    SimulerTjenestepensjonFremtidigInntektDtoV2(LocalDate.of(2029, 2, 1), 300000),
                    SimulerTjenestepensjonFremtidigInntektDtoV2(LocalDate.of(2030, 1, 1), 0)
                ),
                erApoteker = flagg
            )

            OffentligTjenestepensjonFra2025SimuleringSpecMapperV2.fromDto(source) shouldBe
                    OffentligTjenestepensjonFra2025SimuleringSpec(
                        pid = pid,
                        foedselsdato = LocalDate.of(1965, 1, 15),
                        uttaksdato = LocalDate.of(2030, 1, 1),
                        sisteInntekt = 500000,
                        utlandAntallAar = 2,
                        afpErForespurt = flagg,
                        epsHarPensjon = !flagg,
                        epsHarInntektOver2G = flagg,
                        fremtidigeInntekter = listOf(
                            TjenestepensjonInntektSpec(LocalDate.of(2029, 2, 1), 300000),
                            TjenestepensjonInntektSpec(LocalDate.of(2030, 1, 1), 0)
                        ),
                        gjelderApoteker = flagg
                    )
        }
    }

    should("map an empty list of future incomes") {
        val source = SimulerOffentligTjenestepensjonFra2025SpecV2(
            pid = pid.value,
            foedselsdato = LocalDate.of(1965, 1, 15),
            uttaksdato = LocalDate.of(2030, 1, 1),
            sisteInntekt = 0,
            aarIUtlandetEtter16 = 0,
            brukerBaOmAfp = false,
            epsPensjon = false,
            eps2G = false,
            fremtidigeInntekter = emptyList(),
            erApoteker = false
        )

        val result = OffentligTjenestepensjonFra2025SimuleringSpecMapperV2.fromDto(source)

        result.fremtidigeInntekter shouldBe emptyList()
        result.sisteInntekt shouldBe 0
        result.utlandAntallAar shouldBe 0
    }
})
