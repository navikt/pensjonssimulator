package no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v2

import java.time.LocalDate

data class SimulerOffentligTjenestepensjonFra2025SpecV2(
    val pid: String,
    val foedselsdato: LocalDate,
    val uttaksdato: LocalDate,
    val sisteInntekt: Int,
    val aarIUtlandetEtter16: Int,
    val brukerBaOmAfp: Boolean,
    val epsPensjon: Boolean,
    val eps2G: Boolean,
    val fremtidigeInntekter: List<SimulerTjenestepensjonFremtidigInntektDtoV2>,
    val erApoteker: Boolean
)

data class SimulerTjenestepensjonFremtidigInntektDtoV2(val fraOgMed: LocalDate, val aarligInntekt: Int)