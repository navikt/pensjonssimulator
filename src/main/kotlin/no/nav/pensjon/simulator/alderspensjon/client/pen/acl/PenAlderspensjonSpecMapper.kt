package no.nav.pensjon.simulator.alderspensjon.client.pen.acl

import no.nav.pensjon.simulator.alderspensjon.AlderspensjonSpec
import no.nav.pensjon.simulator.alderspensjon.GradertUttakSpec
import no.nav.pensjon.simulator.alderspensjon.InntektSpec

object PenAlderspensjonSpecMapper {

    fun toDto(source: AlderspensjonSpec) =
        PenAlderspensjonSpec(
            personId = source.pid.value,
            gradertUttak = source.gradertUttak?.let(::gradertUttak),
            heltUttakFraOgMedDato = source.heltUttakFom,
            aarIUtlandetEtter16 = source.antallAarUtenlandsEtter16Aar,
            epsPensjon = source.epsHarPensjon,
            eps2G = source.epsHarInntektOver2G,
            fremtidigInntektListe = source.fremtidigInntektListe.map(::inntekt),
            rettTilAfpOffentligDato = source.rettTilAfpOffentligDato
        )

    private fun gradertUttak(source: GradertUttakSpec) =
        PenGradertUttakSpec(
            uttaksgrad = source.uttaksgrad.prosentsats,
            fraOgMedDato = source.fom
        )

    private fun inntekt(source: InntektSpec) =
        PenInntektSpec(
            aarligInntekt = source.aarligBeloep,
            fraOgMedDato = source.fom
        )
}
