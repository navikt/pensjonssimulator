package no.nav.pensjon.simulator.opptjening.client.popp.acl

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Forstegangstjeneste

data class PoppFoerstegangstjeneste(
    val forstegangstjenestePeriodeListe: List<PoppFoerstegangstjenestePeriode> = emptyList()
) {
    fun toGrunnlag() =
        Forstegangstjeneste().apply {
            periodeListe = forstegangstjenestePeriodeListe.map { it.toGrunnlag() }.toMutableList()
        }
}