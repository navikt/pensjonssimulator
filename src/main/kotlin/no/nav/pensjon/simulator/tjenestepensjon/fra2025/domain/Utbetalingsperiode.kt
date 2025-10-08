package no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain

import java.time.LocalDate

data class Utbetalingsperiode(val fom: LocalDate, val maanedligBelop: Int, val ytelseType: String)
