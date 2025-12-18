package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class UtenlandsperiodeForSimuleringV3(
    val land: String,
    val arbeidetIUtland: Boolean = false,
    val periodeFom: LocalDate,
    val periodeTom: LocalDate? = null,
)
