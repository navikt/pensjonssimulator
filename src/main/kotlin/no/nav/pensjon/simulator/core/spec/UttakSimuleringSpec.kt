package no.nav.pensjon.simulator.core.spec

import no.nav.pensjon.simulator.alder.AlderDato
import no.nav.pensjon.simulator.core.krav.UttakGradKode

data class GradertUttakSimuleringSpec(
    val grad: UttakGradKode,
    val uttakFom: AlderDato,
    val aarligInntektBeloep: Int?
)

data class HeltUttakSimuleringSpec(
    val uttakFom: AlderDato,
    val aarligInntektBeloep: Int,
    val inntektTom: AlderDato
)
