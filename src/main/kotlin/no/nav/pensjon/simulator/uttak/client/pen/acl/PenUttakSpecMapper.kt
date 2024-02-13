package no.nav.pensjon.simulator.uttak.client.pen.acl

import no.nav.pensjon.simulator.uttak.InntektSpec
import no.nav.pensjon.simulator.uttak.TidligstMuligUttakSpec

object PenUttakSpecMapper {

    fun toDto(spec: TidligstMuligUttakSpec) =
        PenTidligstMuligUttakSpec(
            pid = spec.pid.value,
            foedselDato = spec.foedselDato,
            uttakGrad = PenUttaksgrad.fromInternalValue(spec.uttakGrad).externalValue,
            rettTilOffentligAfpFom = spec.rettTilOffentligAfpFom,
            antallAarUtenlandsEtter16Aar = spec.antallAarUtenlandsEtter16Aar,
            fremtidigInntektListe = spec.fremtidigInntektListe.map(::egressInntektSpec),
            epsHarPensjon = spec.epsHarPensjon,
            epsHarInntektOver2G = spec.epsHarInntektOver2G
        )

    private fun egressInntektSpec(spec: InntektSpec) =
        PenTmuInntektSpec(
            fom = spec.fom,
            aarligBeloep = spec.aarligBeloep
        )
}
