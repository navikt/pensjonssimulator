package no.nav.pensjon.simulator.core.spec

import no.nav.pensjon.simulator.alder.PensjonAlderDato
import no.nav.pensjon.simulator.core.krav.UttakGradKode

data class GradertUttakSimuleringSpec(
    val grad: UttakGradKode,
    val uttakFom: PensjonAlderDato,
    val aarligInntektBeloep: Int?
)

data class HeltUttakSimuleringSpec(
    val uttakFom: PensjonAlderDato,
    val aarligInntektBeloep: Int,
    val inntektTom: PensjonAlderDato
)
