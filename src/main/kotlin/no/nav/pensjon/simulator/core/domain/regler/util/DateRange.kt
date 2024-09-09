package no.nav.pensjon.simulator.core.domain.regler.util

import java.time.LocalDate

interface DateRange {
    fun range(): ClosedRange<LocalDate>
}
