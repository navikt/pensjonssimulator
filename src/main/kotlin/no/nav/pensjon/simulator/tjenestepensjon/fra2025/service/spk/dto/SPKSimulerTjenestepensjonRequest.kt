package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.dto

import java.time.LocalDate

data class SPKSimulerTjenestepensjonRequest(
    val personId: String,
    val uttaksListe: List<Uttak>,
    val fremtidigInntektListe: List<FremtidigInntekt>,
    val aarIUtlandetEtter16: Int,
    val epsPensjon: Boolean,
    val eps2G: Boolean
)

data class Uttak(
    val ytelseType: String,
    val fraOgMedDato: LocalDate,
    val uttaksgrad: Int?
)

data class FremtidigInntekt(
    val aarligInntekt: Int,
    val fraOgMedDato: LocalDate,
)
