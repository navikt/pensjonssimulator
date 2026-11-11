package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.inntektliste

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.Inntekt
import java.time.LocalDate
import java.time.Month

class InntektListeAggregatorTest : StringSpec({

    "skal opprette inntekter for alle perioder" {
        val spec = InntektListeSpec(
            foedselsdato = foedselsdatoForAlder65,
            inntektFoerFoersteUttak = 1,
            gradertUttak = true,
            simuleringTypeErAfpEtterfAlder = true, // => helt uttak er ved 67 år (2028-11-10)
            inntektUnderGradertUttakBeloep = 2,
            inntektEtterHeltUttakBeloep = 3,
            inntektEtterHeltUttakTom = LocalDate.of(2032, Month.DECEMBER, 31),
            foersteUttakDato = datoForFoersteUttak,
            heltUttakDato = datoForHeltUttak // irrelevant ved 'AFP etterfulgt av AP'
        )

        val result: List<Inntekt> = testAggregator(spec)

        result shouldHaveSize 4
        with(result[0]) {
            fom shouldBe foersteJanuarAaretEtterSisteGyldigeOpptjeningsaar
            beloep shouldBe 1.0
        }
        with(result[1]) {
            fom shouldBe datoForFoersteUttak
            beloep shouldBe 2.0
        }
        with(result[2]) {
            fom shouldBe datoForOvergangFraAfpTilAlderspensjon
            beloep shouldBe 3.0
        }
        with(result[3]) {
            fom shouldBe spec.inntektEtterHeltUttakTom //TODO burde være inntektEtterHeltUttakTom + 1 dag?
            beloep shouldBe 0.0
        }
    }

    "skal opprette inntekter frem til helt uttak" {
        val spec = InntektListeSpec(
            foedselsdato = foedselsdatoForAlder65,
            inntektFoerFoersteUttak = 1,
            gradertUttak = true,
            simuleringTypeErAfpEtterfAlder = true, // => helt uttak er ved 67 år (2028-11-10)
            inntektUnderGradertUttakBeloep = 2,
            inntektEtterHeltUttakBeloep = 0,
            inntektEtterHeltUttakTom = datoForOvergangFraAfpTilAlderspensjon,
            foersteUttakDato = datoForFoersteUttak,
            heltUttakDato = datoForHeltUttak // irrelevant ved 'AFP etterfulgt av AP'
        )

        val result: List<Inntekt> = testAggregator(spec)

        result shouldHaveSize 3
        with(result[0]) {
            fom shouldBe foersteJanuarAaretEtterSisteGyldigeOpptjeningsaar
            beloep shouldBe 1.0
        }
        with(result[1]) {
            fom shouldBe datoForFoersteUttak
            beloep shouldBe 2.0
        }
        with(result[2]) {
            fom shouldBe datoForOvergangFraAfpTilAlderspensjon
            beloep shouldBe 0.0
        }
    }

    "skal opprette inntekter frem til første uttak" {
        val spec = InntektListeSpec(
            foedselsdato = foedselsdatoForAlder65,
            inntektFoerFoersteUttak = 1,
            gradertUttak = true,
            simuleringTypeErAfpEtterfAlder = true, // => helt uttak er ved 67 år (2028-11-10)
            inntektUnderGradertUttakBeloep = 0,
            inntektEtterHeltUttakBeloep = 0,
            inntektEtterHeltUttakTom = datoForOvergangFraAfpTilAlderspensjon,
            foersteUttakDato = datoForFoersteUttak,
            heltUttakDato = datoForHeltUttak // irrelevant ved 'AFP etterfulgt av AP'
        )

        val result: List<Inntekt> = testAggregator(spec)

        result shouldHaveSize 2
        with(result[0]) {
            fom shouldBe foersteJanuarAaretEtterSisteGyldigeOpptjeningsaar
            beloep shouldBe 1.0
        }
        with(result[1]) {
            fom shouldBe datoForFoersteUttak
            beloep shouldBe 0.0
        }
    }

    "skal opprette 0-inntekt hvis ingen inntekter spesifisert" {
        val spec = InntektListeSpec(
            foedselsdato = foedselsdatoForAlder65,
            inntektFoerFoersteUttak = 0,
            gradertUttak = true,
            simuleringTypeErAfpEtterfAlder = true, // => helt uttak er ved 67 år (2028-11-10)
            inntektUnderGradertUttakBeloep = 0,
            inntektEtterHeltUttakBeloep = 0,
            inntektEtterHeltUttakTom = datoForOvergangFraAfpTilAlderspensjon,
            foersteUttakDato = datoForFoersteUttak,
            heltUttakDato = datoForHeltUttak // irrelevant ved 'AFP etterfulgt av AP'
        )

        val result: List<Inntekt> = testAggregator(spec)

        result shouldHaveSize 1
        with(result[0]) {
            fom shouldBe foersteJanuarAaretEtterSisteGyldigeOpptjeningsaar
            beloep shouldBe 0.0
        }
    }

    "skal opprette inntekter frem til 75 år selv om brukeren har oppgitt flere år" {
        val spec = InntektListeSpec(
            foedselsdato = foedselsdatoForAlder65,
            inntektFoerFoersteUttak = 1,
            gradertUttak = true,
            simuleringTypeErAfpEtterfAlder = true, // => helt uttak er ved 67 år (2028-11-10)
            inntektUnderGradertUttakBeloep = 2,
            inntektEtterHeltUttakBeloep = 3,
            inntektEtterHeltUttakTom = LocalDate.of(2048, Month.DECEMBER, 31),
            foersteUttakDato = datoForFoersteUttak,
            heltUttakDato = datoForHeltUttak // irrelevant ved 'AFP etterfulgt av AP'
        )

        val result: List<Inntekt> = testAggregator(spec)

        result shouldHaveSize 4
        with(result[0]) {
            fom shouldBe foersteJanuarAaretEtterSisteGyldigeOpptjeningsaar
            beloep shouldBe 1.0
        }
        with(result[1]) {
            fom shouldBe datoForFoersteUttak
            beloep shouldBe 2.0
        }
        with(result[2]) {
            fom shouldBe datoForOvergangFraAfpTilAlderspensjon
            beloep shouldBe 3.0
        }
        with(result[3]) {
            fom shouldBe foersteDagMaanedenEtterOevreAldersgrenseForOpptjening
            beloep shouldBe 0.0
        }
    }

    "heltuttaksdato skal ikke overskrives ved andre simuleringstyper enn AFP_ETTERF_ALDER" {
        val spec = InntektListeSpec(
            foedselsdato = foedselsdatoForAlder65,
            inntektFoerFoersteUttak = 1,
            gradertUttak = true,
            simuleringTypeErAfpEtterfAlder = false, // => heltUttakDato er relevant
            inntektUnderGradertUttakBeloep = 2,
            inntektEtterHeltUttakBeloep = 3,
            inntektEtterHeltUttakTom = LocalDate.of(2036, Month.DECEMBER, 31),
            foersteUttakDato = datoForFoersteUttak,
            heltUttakDato = datoForHeltUttak
        )

        val result: List<Inntekt> = testAggregator(spec)

        result shouldHaveSize 4
        with(result[2]) {
            fom shouldBe datoForHeltUttak
            beloep shouldBe 3.0
        }
    }

    "skal bruke inntektEtterHeltUttakBeloep etter uttak ved annen simuleringstype enn AFP_ETTERF_ALDER og ikke gradertUttak" {
        val spec = InntektListeSpec(
            foedselsdato = foedselsdatoForAlder65,
            inntektFoerFoersteUttak = 1,
            gradertUttak = false, // ugradert uttak
            simuleringTypeErAfpEtterfAlder = false,
            inntektUnderGradertUttakBeloep = 2, // irrelevant ved ugradert uttak
            inntektEtterHeltUttakBeloep = 3,
            inntektEtterHeltUttakTom = foersteDagMaanedenEtterOevreAldersgrenseForOpptjening,
            foersteUttakDato = datoForFoersteUttak,
            heltUttakDato = datoForHeltUttak // irrelevant ved ugradert uttak
        )

        val result: List<Inntekt> = testAggregator(spec)

        result shouldHaveSize 3
        with(result[1]) {
            fom shouldBe datoForFoersteUttak
            beloep shouldBe 3.0
        }
    }

    "skal IKKE legge til like beløp i inntektliste i sammenhengende periode" {
        val spec = InntektListeSpec(
            foedselsdato = foedselsdatoForAlder65,
            inntektFoerFoersteUttak = 1,
            gradertUttak = true,
            simuleringTypeErAfpEtterfAlder = true, // => helt uttak er ved 67 år (2028-11-10)
            inntektUnderGradertUttakBeloep = 1,
            inntektEtterHeltUttakBeloep = 1,
            inntektEtterHeltUttakTom = LocalDate.of(2031, Month.DECEMBER, 31),
            foersteUttakDato = datoForFoersteUttak,
            heltUttakDato = datoForHeltUttak // irrelevant ved 'AFP etterfulgt av AP'
        )

        val result: List<Inntekt> = testAggregator(spec)

        result shouldHaveSize 2
        with(result[0]) {
            fom shouldBe foersteJanuarAaretEtterSisteGyldigeOpptjeningsaar
            beloep shouldBe 1.0
        }
        with(result[1]) {
            fom shouldBe spec.inntektEtterHeltUttakTom
            beloep shouldBe 0.0
        }
    }

    "inntekt skal stoppes ved 75 år ved manglende heltuttaksdato og annen simuleringstype enn AFP_ETTERF_ALDER" {
        val spec = InntektListeSpec(
            foedselsdato = foedselsdatoForAlder65,
            inntektFoerFoersteUttak = 1,
            gradertUttak = true,
            simuleringTypeErAfpEtterfAlder = false, // => heltUttakDato er relevant
            inntektUnderGradertUttakBeloep = 1,
            inntektEtterHeltUttakBeloep = 0,
            inntektEtterHeltUttakTom = LocalDate.of(2028, Month.NOVEMBER, 30),
            foersteUttakDato = datoForFoersteUttak,
            heltUttakDato = null // => dato ved 75 år brukes som default
        )

        val result: List<Inntekt> = testAggregator(spec)

        result shouldHaveSize 2
        with(result[0]) {
            fom shouldBe foersteJanuarAaretEtterSisteGyldigeOpptjeningsaar
            beloep shouldBe 1.0
        }
        with(result[1]) {
            fom shouldBe foersteDagMaanedenEtterOevreAldersgrenseForOpptjening
            beloep shouldBe 0.0
        }
    }
})

private const val SISTE_GYLDIGE_OPPTJENINGSAAR = 2024 // 2 år før dagens dato

/**
 * 65 år før 'dagens dato'.
 */
private val foedselsdatoForAlder65 =
    LocalDate.of(1961, Month.NOVEMBER, 10)

private val datoForFoersteUttak: LocalDate =
    LocalDate.of(2025, Month.NOVEMBER, 1)

private val foersteJanuarAaretEtterSisteGyldigeOpptjeningsaar: LocalDate =
    LocalDate.of(2025, Month.JANUARY, 1)

/**
 * Bruker en fast 'dagens dato' for å gjøre testene uavhengig av faktisk tid.
 */
private val dagensDato: LocalDate =
    LocalDate.of(2026, Month.NOVEMBER, 10)

private val datoForHeltUttak: LocalDate =
    LocalDate.of(2026, Month.DECEMBER, 1)

/**
 *  1. dag i måneden etter fødselsdato + 67 år (alder ved overgang fra AFP til alderspensjon)
 */
private val datoForOvergangFraAfpTilAlderspensjon: LocalDate =
    LocalDate.of(2028, Month.DECEMBER, 1)

/**
 *  1. dag i måneden etter fødselsdato + 75 år (øvre aldersgrense for opptjening)
 */
private val foersteDagMaanedenEtterOevreAldersgrenseForOpptjening: LocalDate =
    LocalDate.of(2036, Month.DECEMBER, 1)

private fun testAggregator(spec: InntektListeSpec): List<Inntekt> =
    InntektListeAggregator.aggregate(
        spec,
        actualTime = dagensDato,
        sisteGyldigeOpptjeningsaar = SISTE_GYLDIGE_OPPTJENINGSAAR
    )