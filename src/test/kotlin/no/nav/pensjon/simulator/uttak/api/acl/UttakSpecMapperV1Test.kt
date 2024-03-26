package no.nav.pensjon.simulator.uttak.api.acl

import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.uttak.GradertUttakSpec
import no.nav.pensjon.simulator.uttak.InntektSpec
import no.nav.pensjon.simulator.uttak.TidligstMuligUttakSpec
import no.nav.pensjon.simulator.uttak.UttakGrad
import org.junit.jupiter.api.Test

import java.time.LocalDate

class UttakSpecMapperV1Test {

    @Test
    fun `fromSpecV1 maps from data transfer object to domain object`() {
        val dto = TidligstMuligUttakSpecV1(
            personId = "12906498357",
            fodselsdato = LocalDate.of(1964, 5, 6),
            uttaksgrad = 50,
            heltUttakFraOgMedDato = LocalDate.of(2030, 1, 1),
            rettTilAfpOffentligDato = LocalDate.of(2028, 9, 10),
            fremtidigInntektListe = listOf(
                UttakInntektSpecV1(arligInntekt = 123000, fraOgMedDato = LocalDate.of(2026, 7, 8)),
                UttakInntektSpecV1(arligInntekt = 0, fraOgMedDato = LocalDate.of(2032, 3, 4))
            )
        )

        val domainObject: TidligstMuligUttakSpec = UttakSpecMapperV1.fromSpecV1(dto)

        val expected = TidligstMuligUttakSpec(
            pid = Pid("12906498357"),
            foedselDato = LocalDate.of(1964, 5, 6),
            gradertUttak = GradertUttakSpec(
                grad = UttakGrad.FEMTI_PROSENT,
                heltUttakFom = LocalDate.of(2030, 1, 1)
            ),
            rettTilOffentligAfpFom = LocalDate.of(2028, 9, 10),
            antallAarUtenlandsEtter16Aar = 0,
            fremtidigInntektListe = listOf(
                InntektSpec(fom = LocalDate.of(2026, 7, 8), aarligBeloep = 123000),
                InntektSpec(fom = LocalDate.of(2032, 3, 4), aarligBeloep = 0)
            ),
            epsHarPensjon = false,
            epsHarInntektOver2G = false
        )

        domainObject shouldBe expected
    }
}
