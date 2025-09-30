package no.nav.pensjon.simulator.tjenestepensjon.fra2025.dto.request

import java.time.LocalDate

data class SimulerTjenestepensjonRequestDto(
    val pid: String,
    val foedselsdato: LocalDate,
    val uttaksdato: LocalDate,
    val sisteInntekt: Int,
    val aarIUtlandetEtter16: Int,
    val brukerBaOmAfp: Boolean,
    val epsPensjon: Boolean,
    val eps2G: Boolean,
    val fremtidigeInntekter: List<SimulerTjenestepensjonFremtidigInntektDto>? = null, //kun i V2
    val erApoteker: Boolean
)

data class SimulerTjenestepensjonFremtidigInntektDto(val fraOgMed: LocalDate, val aarligInntekt: Int)