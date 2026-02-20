package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.marshalling.response

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.acl.Utvidelse
import java.time.LocalDate

data class StillingsprosentDto(
    var datoFom: LocalDate,
    var datoTom: LocalDate?,
    var stillingsprosent: Double,
    var aldersgrense: Int,
    var faktiskHovedlonn: String?,
    var stillingsuavhengigTilleggslonn: String?,
    var utvidelse: Utvidelse.StillingsprosentUtvidelse1?
)