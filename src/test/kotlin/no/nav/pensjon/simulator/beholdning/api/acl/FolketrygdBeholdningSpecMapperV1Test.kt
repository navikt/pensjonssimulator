package no.nav.pensjon.simulator.beholdning.api.acl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.beholdning.FolketrygdBeholdningSpec
import no.nav.pensjon.simulator.beholdning.InntektSpec
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

class FolketrygdBeholdningSpecMapperV1Test : FunSpec({

    test("'fromSpecV1' maps from data transfer object to domain object") {
        val dto = FolketrygdBeholdningSpecV1(
            personId = "12906498357",
            uttaksdato = "2030-01-02",
            fremtidigInntektListe = listOf(
                BeholdningInntektSpecV1(arligInntekt = 123000, fraOgMedDato = "2026-07-08"),
                BeholdningInntektSpecV1(arligInntekt = 0, fraOgMedDato = "2032-03-04")
            ),
            arIUtlandetEtter16 = 1,
            epsPensjon = true,
            eps2G = false
        )

        val domainObject: FolketrygdBeholdningSpec = FolketrygdBeholdningSpecMapperV1.fromSpecV1(dto)

        domainObject shouldBe FolketrygdBeholdningSpec(
            pid = Pid("12906498357"),
            uttakFom = LocalDate.of(2030, 1, 2),
            fremtidigInntektListe = listOf(
                InntektSpec(inntektAarligBeloep = 123000, inntektFom = LocalDate.of(2026, 7, 8)),
                InntektSpec(inntektAarligBeloep = 0, inntektFom = LocalDate.of(2032, 3, 4))
            ),
            antallAarUtenlandsEtter16Aar = 1,
            epsHarPensjon = true,
            epsHarInntektOver2G = false
        )
    }
})
