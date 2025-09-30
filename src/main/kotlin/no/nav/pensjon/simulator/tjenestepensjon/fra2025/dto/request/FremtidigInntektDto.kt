package no.nav.pensjon.simulator.tjenestepensjon.fra2025.dto.request

import java.time.LocalDate

data class FremtidigInntektDto(val aarligInntekt: Int, val fraOgMedDato: LocalDate)
