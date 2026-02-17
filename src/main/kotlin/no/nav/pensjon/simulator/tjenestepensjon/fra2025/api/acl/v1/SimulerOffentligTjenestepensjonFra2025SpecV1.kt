package no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1

import java.time.LocalDate

data class SimulerOffentligTjenestepensjonFra2025SpecV1(
    val pid: String,
    val foedselsdato: LocalDate,
    val uttaksdato: LocalDate,
    val sisteInntekt: Int,
    val aarIUtlandetEtter16: Int,
    val brukerBaOmAfp: Boolean,
    val epsPensjon: Boolean,
    val eps2G: Boolean,
    val fremtidigeInntekter: List<SimulerTjenestepensjonFremtidigInntektDto>,
    val erApoteker: Boolean
)

data class SimulerTjenestepensjonFremtidigInntektDto(val fraOgMed: LocalDate, val aarligInntekt: Int)