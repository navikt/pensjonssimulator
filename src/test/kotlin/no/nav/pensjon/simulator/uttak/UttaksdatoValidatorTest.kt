package no.nav.pensjon.simulator.uttak

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.exception.BadSpecException
import no.nav.pensjon.simulator.normalder.Aldersgrenser
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.normalder.VerdiStatus
import java.time.LocalDate

class UttaksdatoValidatorTest : FunSpec({

    test("verifyUttakFom: uttakFom must be the first day in a month") {
        shouldThrow<BadSpecException> {
            UttaksdatoValidator(
                normalderService = arrangeNormalder(LocalDate.of(1970, 1, 1)),
                time = { LocalDate.of(2025, 1, 1) }
            ).verifyUttakFom(
                uttakFom = LocalDate.of(2035, 1, 2), // ikke 1. dag i måneden
                foedselsdato = LocalDate.of(1970, 1, 1)
            )
        }.message shouldBe "uttakFom must be the first day in a month"
    }

    test("verifyUttakFom: uttakFom cannot be earlier than first month after user turns 62") {
        shouldThrow<BadSpecException> {
            UttaksdatoValidator(
                normalderService = arrangeNormalder(LocalDate.of(1970, 1, 1)),
                time = { LocalDate.of(2025, 1, 1) }
            ).verifyUttakFom(
                uttakFom = LocalDate.of(2030, 2, 1), // alder 60 år 0 måneder (mangler "noen timer" på 1 måned)
                foedselsdato = LocalDate.of(1970, 1, 1)
            )
        }.message shouldBe "uttakFom cannot be earlier than first month after user turns 62 år"
    }

    test("verifyUttakFom: uttakFom cannot be later than first month after user turns 75") {
        shouldThrow<BadSpecException> {
            UttaksdatoValidator(
                normalderService = arrangeNormalder(LocalDate.of(1970, 1, 1)),
                time = { LocalDate.of(2025, 1, 1) }
            ).verifyUttakFom(
                uttakFom = LocalDate.of(2045, 3, 1), // alder 75 år 1 måned (mangler "noen timer" på 2 måneder)
                foedselsdato = LocalDate.of(1970, 1, 1)
            )
        }.message shouldBe "uttakFom cannot be later than first month after user turns 75 år"
    }

    test("verifyUttakFom: uttakFom must be after today") {
        shouldThrow<BadSpecException> {
            UttaksdatoValidator(
                normalderService = arrangeNormalder(LocalDate.of(1970, 1, 1)),
                time = { LocalDate.of(2036, 1, 1) } // "i dag"
            ).verifyUttakFom(
                uttakFom = LocalDate.of(2035, 12, 1), // 1 måned før "i dag"
                foedselsdato = LocalDate.of(1970, 1, 1)
            )
        }.message shouldBe "uttakFom must be after today"
    }
})

private fun arrangeNormalder(foedselsdato: LocalDate): NormertPensjonsalderService =
    mockk<NormertPensjonsalderService>().apply {
        every { aldersgrenser(foedselsdato) } returns
                Aldersgrenser(
                    aarskull = 1965,
                    nedreAlder = Alder(62, 0),
                    normalder = Alder(67, 0),
                    oevreAlder = Alder(75, 0),
                    verdiStatus = VerdiStatus.FAST
                )
    }
