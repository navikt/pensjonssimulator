package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.nav

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.inntekt.LoependeInntekt
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.OffentligTjenestepensjonFra2025SimuleringSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonInntektSpec
import java.time.LocalDate

class AfpYtelseMapperTest : FunSpec({
    val request = OffentligTjenestepensjonFra2025SimuleringSpec(
        pid = Pid("12345678910"),
        foedselsdato = LocalDate.of(1963, 2, 5),
        uttaksdato = LocalDate.of(2026, 3, 1),
        sisteInntekt = 500000,
        utlandAntallAar = 0,
        afpErForespurt = true,
        epsHarPensjon = false,
        epsHarInntektOver2G = false,
        fremtidigeInntekter = listOf(
            TjenestepensjonInntektSpec(LocalDate.of(2026, 3, 1), 300000),
            TjenestepensjonInntektSpec(LocalDate.of(2030, 1, 1), 0)
        ),
        gjelderApoteker = false
    )

    test("maps request fields and future incomes") {
        AfpYtelseMapper.toLivsvarigOffentligAfpSpec(request) shouldBe LivsvarigOffentligAfpSpec(
            pid = request.pid,
            foedselsdato = request.foedselsdato,
            fom = request.uttaksdato,
            fremtidigInntektListe = listOf(
                LoependeInntekt(aarligBeloep = 300000, fom = LocalDate.of(2026, 3, 1)),
                LoependeInntekt(aarligBeloep = 0, fom = LocalDate.of(2030, 1, 1))
            )
        )
    }

    test("preserves an empty future income list") {
        AfpYtelseMapper.toLivsvarigOffentligAfpSpec(
            request.copy(fremtidigeInntekter = emptyList())
        ).fremtidigInntektListe shouldBe emptyList()
    }
})
