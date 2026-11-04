package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.nav

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.afp.offentlig.livsvarig.LivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.inntekt.LoependeInntekt
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.OffentligTjenestepensjonFra2025SimuleringSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonInntektSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.KlpMapper
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.SpkMapper
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand.StorebrandMapper
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

    val fremtidigeInntekter = { spec: OffentligTjenestepensjonFra2025SimuleringSpec ->
        spec.fremtidigeInntekter.map { FremtidigInntekt(aarligInntektBeloep = it.aarligInntekt, fom = it.fom) }
    }

    test("maps request fields and future incomes") {
        AfpYtelseMapper.toLivsvarigOffentligAfpSpec(request, fremtidigeInntekter) shouldBe LivsvarigOffentligAfpSpec(
            pid = request.pid,
            foedselsdato = request.foedselsdato,
            fom = request.uttaksdato,
            fremtidigInntektListe = listOf(
                FremtidigInntekt(aarligInntektBeloep = 300000, fom = LocalDate.of(2026, 3, 1)),
                FremtidigInntekt(aarligInntektBeloep = 0, fom = LocalDate.of(2030, 1, 1))
            )
        )
    }

    test("preserves an empty future income list") {
        AfpYtelseMapper.toLivsvarigOffentligAfpSpec(
            request.copy(fremtidigeInntekter = emptyList()),
            fremtidigeInntekter
        ).fremtidigInntektListe shouldBe emptyList()
    }

    listOf(
        Triple("Storebrand", StorebrandMapper::inntekter, LocalDate.now().minusYears(1).withDayOfYear(1)),
        Triple("SPK", SpkMapper::inntekter, LocalDate.now().minusYears(1).withDayOfYear(1)),
        Triple("KLP", KlpMapper::inntekter, LocalDate.now())
    ).forEach { (leverandoer, inntekter, forventetFom) ->
        test("uses $leverandoer income function including current income") {
            AfpYtelseMapper.toLivsvarigOffentligAfpSpec(request, inntekter).fremtidigInntektListe shouldBe listOf(
                FremtidigInntekt(aarligInntektBeloep = 500000, fom = forventetFom),
                FremtidigInntekt(aarligInntektBeloep = 300000, fom = LocalDate.of(2026, 3, 1)),
                FremtidigInntekt(aarligInntektBeloep = 0, fom = LocalDate.of(2030, 1, 1))
            )
        }
    }
})
