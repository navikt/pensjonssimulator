package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class UtenlandsperiodeForSimuleringV2(
    val land: String,
    val arbeidetIUtland: Boolean = false,
    val periodeFom: LocalDate,
    val periodeTom: LocalDate? = null,
)
