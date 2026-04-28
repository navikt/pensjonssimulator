package no.nav.pensjon.simulator.core.inntekt

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.InntekttypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Inntektsgrunnlag
import java.time.LocalDate

class InntektsgrunnlagValidityTest : FunSpec({

    test("'test' should return false when bruk is false") {
        InntektsgrunnlagValidity(
            sakType = null,
            virkDatoFom = null,
            virkDatoTom = null,
            periodiserFomTomDatoUtenUnntak = false
        ).test(Inntektsgrunnlag().apply { bruk = false }) shouldBe false
    }

    test("'test' should return true for AFP-sak") {
        InntektsgrunnlagValidity(
            sakType = null,
            virkDatoFom = null,
            virkDatoTom = null,
            periodiserFomTomDatoUtenUnntak = false
        ).test(Inntektsgrunnlag().apply { bruk = true }) shouldBe true
    }

    test("'test' should return true when pensjonsgivende inntekt uten tidsbegrensning") {
        InntektsgrunnlagValidity(
            sakType = null,
            virkDatoFom = null,
            virkDatoTom = null,
            periodiserFomTomDatoUtenUnntak = false
        ).test(Inntektsgrunnlag().apply {
            bruk = true
            inntektTypeEnum = InntekttypeEnum.PGI
            fomLd = null
            tomLd = null
        }) shouldBe true
    }

    test("'test' should return true when tidsbegrenset pensjonsgivende inntekt innenfor aktuelt tidsrom") {
        InntektsgrunnlagValidity(
            sakType = null,
            virkDatoFom = LocalDate.of(2021, 6, 1), // => start = 2019-01-01
            virkDatoTom = LocalDate.of(2022, 1, 1), // => end = 2022-12-31
            periodiserFomTomDatoUtenUnntak = false
        ).test(Inntektsgrunnlag().apply {
            bruk = true
            inntektTypeEnum = InntekttypeEnum.PGI
            fomLd = LocalDate.of(2018, 1, 1)
            tomLd = LocalDate.of(2019, 2, 1) // overlap: 2019-01-01 to 2019-02-01
        }) shouldBe true
    }

    test("'test' should return false when tidsbegrenset pensjonsgivende inntekt utenfor aktuelt tidsrom") {
        InntektsgrunnlagValidity(
            sakType = null,
            virkDatoFom = LocalDate.of(2021, 6, 1), // => start = 2019-01-01
            virkDatoTom = LocalDate.of(2022, 1, 1), // => end = 2022-12-31
            periodiserFomTomDatoUtenUnntak = false
        ).test(Inntektsgrunnlag().apply {
            bruk = true
            inntektTypeEnum = InntekttypeEnum.PGI
            fomLd = LocalDate.of(2018, 1, 1)
            tomLd = LocalDate.of(2018, 12, 1) // no overlap
        }) shouldBe false
    }
})
