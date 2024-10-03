package no.nav.pensjon.simulator.generelt.client.pen.acl

import no.nav.pensjon.simulator.generelt.GenerelleDataSpec
import no.nav.pensjon.simulator.generelt.InkluderingSpec
import no.nav.pensjon.simulator.generelt.PeriodeSpec

object PenGenerelleDataSpecMapper {

    fun toDto(spec: GenerelleDataSpec) =
        PenGenerelleDataSpec(
            pid = spec.pid?.value,
            anonymFoedselDato = spec.foedselDato,
            foersteVirkning = spec.virkningFom,
            satsPeriode = spec.satsPeriode?.let(::periode),
            inkludering = inkludering(spec.inkludering)
        )

    private fun periode(source: PeriodeSpec) =
        PenPeriodeSpec(
            fomAar = source.fomAar,
            tomAar = source.tomAar
        )

    private fun inkludering(source: InkluderingSpec) =
        PenDataInkluderingSpec(
            afpSatser = source.afpSatser,
            delingstall = source.delingstall,
            forholdstall = source.forholdstall
        )
}
