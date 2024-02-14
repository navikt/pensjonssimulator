package no.nav.pensjon.simulator.uttak.client.pen.acl

import no.nav.pensjon.simulator.uttak.GradertUttakSpec
import no.nav.pensjon.simulator.uttak.InntektSpec
import no.nav.pensjon.simulator.uttak.TidligstMuligUttakSpec

object PenUttakSpecMapper {

    fun toDto(spec: TidligstMuligUttakSpec) =
        PenTidligstMuligUttakSpec(
            pid = spec.pid.value,
            foedselsdato = spec.foedselDato,
            gradertUttak = spec.gradertUttak?.let(::penGradertUttakSpec),
            rettTilOffentligAfpFom = spec.rettTilOffentligAfpFom,
            antallAarUtenlandsEtter16Aar = spec.antallAarUtenlandsEtter16Aar,
            fremtidigInntektListe = spec.fremtidigInntektListe.map(::penInntektSpec),
            epsHarPensjon = spec.epsHarPensjon,
            epsHarInntektOver2G = spec.epsHarInntektOver2G
        )

    private fun penGradertUttakSpec(spec: GradertUttakSpec) =
        PenTmuGradertUttakSpec(
            PenUttaksgrad.fromInternalValue(spec.grad).externalValue,
            spec.heltUttakFom
        )

    private fun penInntektSpec(spec: InntektSpec) =
        PenTmuInntektSpec(
            fom = spec.fom,
            aarligBeloep = spec.aarligBeloep
        )
}
