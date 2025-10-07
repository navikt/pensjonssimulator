package no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.client.acl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.afp.offentlig.fra2025.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.SimulerAfpOffentligLivsvarigBeholdningsperiode
import no.nav.pensjon.simulator.inntekt.Inntekt
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

class SimulerLivsvarigOffentligAfpBeholdningsgrunnlagMapperTest : FunSpec({

    test("toDto maps pid, fom and income periods") {
        val spec = LivsvarigOffentligAfpSpec(
            pid = Pid("12345678910"),
            foedselsdato = LocalDate.now(),
            fom = LocalDate.parse("2025-03-01"),
            fremtidigInntektListe = listOf(
                Inntekt(fom = LocalDate.parse("2025-10-01"), aarligBeloep = 500000),
                Inntekt(fom = LocalDate.parse("2026-10-01"), aarligBeloep = 520000),
            )
        )

        val dto: SimulerLivsvarigOffentligAfpBeholdningsgrunnlagSpec = SimulerLivsvarigOffentligAfpBeholdningsgrunnlagMapper.toDto(spec)

        dto.personId shouldBe Pid("12345678910")
        dto.uttaksDato shouldBe LocalDate.parse("2025-03-01")
        dto.fremtidigInntektListe shouldHaveSize 2
        dto.fremtidigInntektListe[0].fraOgMedDato shouldBe LocalDate.parse("2025-10-01")
        dto.fremtidigInntektListe[0].arligInntekt shouldBe 500_000
        dto.fremtidigInntektListe[1].fraOgMedDato shouldBe LocalDate.parse("2026-10-01")
        dto.fremtidigInntektListe[1].arligInntekt shouldBe 520_000
    }

    test("toDto handles empty income list") {
        val spec = LivsvarigOffentligAfpSpec(
            pid = Pid("12345678910"),
            foedselsdato = LocalDate.now(),
            fom = LocalDate.parse("2025-03-01"),
            fremtidigInntektListe = emptyList()
        )

        val dto = SimulerLivsvarigOffentligAfpBeholdningsgrunnlagMapper.toDto(spec)

        dto.personId shouldBe Pid("12345678910")
        dto.uttaksDato shouldBe LocalDate.parse("2025-03-01")
        dto.fremtidigInntektListe.shouldBeEmpty()
    }

    test("fromDto maps response periods to domain periods") {
        val response = SimulerLivsvarigOffentligAfpBeholdningsgrunnlagResult(
            afpBeholdningsgrunnlag = listOf(
                Beholdningsperiode(belop = 1000, fraOgMedDato = LocalDate.parse("2025-03-01")),
                Beholdningsperiode(belop = 2000, fraOgMedDato = LocalDate.parse("2026-03-01")),
            )
        )

        val periods: List<SimulerAfpOffentligLivsvarigBeholdningsperiode> =
            SimulerLivsvarigOffentligAfpBeholdningsgrunnlagMapper.fromDto(response)

        periods shouldHaveSize 2
        periods[0].pensjonsbeholdning shouldBe 1000
        periods[0].fom shouldBe LocalDate.parse("2025-03-01")
        periods[1].pensjonsbeholdning shouldBe 2000
        periods[1].fom shouldBe LocalDate.parse("2026-03-01")
    }

    test("fromDto handles empty response list") {
        val response = SimulerLivsvarigOffentligAfpBeholdningsgrunnlagResult(
            afpBeholdningsgrunnlag = emptyList()
        )

        val periods = SimulerLivsvarigOffentligAfpBeholdningsgrunnlagMapper.fromDto(response)

        periods.shouldBeEmpty()
    }
})