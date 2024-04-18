package no.nav.pensjon.simulator.beholdning.client.pen.acl

import no.nav.pensjon.simulator.beholdning.FolketrygdBeholdningSpec
import no.nav.pensjon.simulator.beholdning.InntektSpec

object PenFolketrygdBeholdningSpecMapper {
    fun toDto(source: FolketrygdBeholdningSpec) =
        PenFolketrygdBeholdningSpec(
            pid = source.pid.value,
            uttakFom = source.uttakFom,
            aarUtenlandsEtter16Aar = source.antallAarUtenlandsEtter16Aar,
            epsHarPensjon = source.epsHarPensjon,
            epsHarInntektOver2G = source.epsHarInntektOver2G,
            fremtidigInntektListe = source.fremtidigInntektListe.map(::inntekt)
        )

    private fun inntekt(source: InntektSpec) =
        PenInntektSpec(
            aarligInntekt = source.inntektAarligBeloep,
            fom = source.inntektFom
        )
}
