package no.nav.pensjon.simulator.beholdning.client.pen.acl

import no.nav.pensjon.simulator.beholdning.FolketrygdBeholdningSpec
import no.nav.pensjon.simulator.beholdning.InntektSpec

object PenFolketrygdBeholdningSpecMapper {
    fun toDto(source: FolketrygdBeholdningSpec) =
        PenFolketrygdBeholdningSpec(
            source.pid.value,
            source.uttakFom,
            source.fremtidigInntektListe.map(::inntekt),
            source.antallAarUtenlandsEtter16Aar,
            source.epsHarPensjon,
            source.epsHarInntektOver2G
        )

    private fun inntekt(source: InntektSpec) =
        PenInntektSpec(
            source.inntektAarligBeloep,
            source.inntektFom
        )
}
