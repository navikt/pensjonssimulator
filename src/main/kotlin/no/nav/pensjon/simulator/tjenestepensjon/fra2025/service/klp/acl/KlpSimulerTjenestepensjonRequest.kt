package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl

import java.time.LocalDate

data class KlpSimulerTjenestepensjonRequest(
    val personId: String,
    val uttaksListe: List<Uttak>,
    val fremtidigInntektsListe: List<FremtidigInntekt>,
    val arIUtlandetEtter16: Int,
    val epsPensjon: Boolean,
    val eps2G: Boolean
)

data class Uttak(
    val ytelseType: String,
    val fraOgMedDato: LocalDate,
    val uttaksgrad: Int
)

data class FremtidigInntekt(
    val fraOgMedDato: LocalDate,
    val arligInntekt: Int
)
