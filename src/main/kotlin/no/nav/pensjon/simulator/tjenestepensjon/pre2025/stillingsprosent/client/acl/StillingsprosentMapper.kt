package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.acl

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.Stillingsprosent
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.StillingsprosentUtvidelse
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.acl.Utvidelse
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.response.StillingsprosentDto

object StillingsprosentMapper {

    fun fromDto(source: StillingsprosentDto) =
        Stillingsprosent(
            datoFom = source.datoFom,
            datoTom = source.datoTom,
            stillingsprosent = source.stillingsprosent,
            aldersgrense = source.aldersgrense,
            faktiskHovedlonn = source.faktiskHovedlonn,
            stillingsuavhengigTilleggslonn = source.stillingsuavhengigTilleggslonn,
            utvidelse = source.utvidelse?.let(::utvidelse)
        )

    fun utvidelse(source: Utvidelse.StillingsprosentUtvidelse1) =
        StillingsprosentUtvidelse(listOf(source), source.otherAttributes.mapKeys { it.key.namespaceURI })
}
