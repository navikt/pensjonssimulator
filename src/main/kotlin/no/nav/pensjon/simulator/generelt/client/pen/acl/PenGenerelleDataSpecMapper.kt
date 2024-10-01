package no.nav.pensjon.simulator.generelt.client.pen.acl

import no.nav.pensjon.simulator.generelt.GenerelleDataSpec

object PenGenerelleDataSpecMapper {

    fun toDto(spec: GenerelleDataSpec) =
        PenGenerelleDataSpec(
            pid = null,
            anonymFoedselDato = spec.foedselDato,
            foersteVirkning = spec.virkningFom,
            satsPeriode = null,
            inkludering = PenDataInkluderingSpec(afpSatser = false, delingstall = true, forholdstall = false)
        )
}
