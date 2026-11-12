package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.inntektliste

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.Month

class InntektListeSpecTest : ShouldSpec({

    context("funksjonen 'inntektTom'") {
        context("gyldig inntekt etter helt uttak") {
            should("gi sluttdatoen for inntekt etter helt uttak") {
                InntektListeSpec(
                    foedselsdato = foedselsdato,
                    inntektFoerFoersteUttak = 99000,
                    gradertUttak = false,
                    simuleringTypeErAfpEtterfAlder = false,
                    inntektUnderGradertUttakBeloep = 0,
                    inntektEtterHeltUttakBeloep = 123000,
                    inntektEtterHeltUttakTom = LocalDate.of(2030, Month.AUGUST, 31),
                    foersteUttakDato = LocalDate.of(2028, Month.APRIL, 30),
                    heltUttakDato = null
                ).inntektTom() shouldBe LocalDate.of(2030, Month.AUGUST, 31)
            }
        }

        context("null inntekt etter helt uttak") {
            should("gi siste mulige opptjeningsdato") {
                InntektListeSpec(
                    foedselsdato = foedselsdato,
                    inntektFoerFoersteUttak = 99000,
                    gradertUttak = false,
                    simuleringTypeErAfpEtterfAlder = false,
                    inntektUnderGradertUttakBeloep = 0,
                    inntektEtterHeltUttakBeloep = 0,
                    inntektEtterHeltUttakTom = LocalDate.of(2030, Month.AUGUST, 31),
                    foersteUttakDato = LocalDate.of(2028, Month.APRIL, 30),
                    heltUttakDato = null
                ).inntektTom() shouldBe sisteMuligeOpptjeningsdato
            }
        }

        context("inntekt slutter etter siste mulige opptjeningsdato") {
            should("gi siste mulige opptjeningsdato") {
                InntektListeSpec(
                    foedselsdato = foedselsdato,
                    inntektFoerFoersteUttak = 99000,
                    gradertUttak = false,
                    simuleringTypeErAfpEtterfAlder = false,
                    inntektUnderGradertUttakBeloep = 0,
                    inntektEtterHeltUttakBeloep = 123000,
                    inntektEtterHeltUttakTom = LocalDate.of(2040, Month.JULY, 31),
                    foersteUttakDato = LocalDate.of(2028, Month.APRIL, 30),
                    heltUttakDato = null
                ).inntektTom() shouldBe sisteMuligeOpptjeningsdato
            }
        }
    }
})

private val foedselsdato: LocalDate = LocalDate.of(1965, Month.JUNE, 7)

/**
 * Siste mulige dato for opptjening er 1. dag i måneden etter fylte 75 år.
 * I dette tilfellet: 1965-06-07 + 75 år = 2040-06-07 -> 2040-07-01
 */
private val sisteMuligeOpptjeningsdato: LocalDate = LocalDate.of(2040, Month.JULY, 1)