package no.nav.pensjon.simulator.alderspensjon.alternativ

import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.search.ValueAssessment
import java.time.LocalDate

/**
 * Inneholder input-parametre og output-resultat for en alternativ simulering.
 */
data class AlternativSimuleringResult(
    override val valueIsGood: Boolean,
    val simulertPensjon: SimulatorOutput?,
    val usedParameters: AlternativSimuleringSpec,
    val uttakAlderTransition: Boolean = false
) : ValueAssessment

data class AlternativSimuleringSpec(
    val gradertUttakFom: LocalDate?,
    val gradertUttakAlderIndex: Int?,
    val uttakGrad: UttakGradKode,
    val heltUttakFom: LocalDate,
    val heltUttakAlderIndex: Int,
    val inntektEtterHeltUttakAntallAar: Int? = null
)
