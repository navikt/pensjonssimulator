package no.nav.pensjon.simulator.tjenestepensjon.fra2025.dto.request

import java.time.LocalDate

data class UttakDto(val ytelseType: String, val fraOgMedDato: LocalDate, val uttaksgrad: Int)
