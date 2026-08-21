package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.inntektliste

import java.time.LocalDate

data class InntektListeSpec(
    val foedselsdato: LocalDate,
    val inntektFoerFoersteUttak: Int,
    val gradertUttak: Boolean,
    val simuleringTypeErAfpEtterfAlder: Boolean,
    val inntektUnderGradertUttakBeloep: Int,
    val inntektEtterHeltUttakBeloep: Int,

    @Deprecated("Bruk inntektEtterHeltUttakTom")
    val inntektEtterHeltUttakAntallAar: Int?,

    val inntektEtterHeltUttakTom: LocalDate?,
    val foersteUttakDato: LocalDate,
    val heltUttakDato: LocalDate?
)