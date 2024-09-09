package no.nav.pensjon.simulator.core.util

import java.time.LocalDate
import java.util.*

fun Date?.toLocalDate(): LocalDate? {
    if (this == null) {
        return null
    }

    val calendar = Calendar.getInstance().also { it.time = this }

    return LocalDate.of(
        calendar[Calendar.YEAR],
        calendar[Calendar.MONTH] + 1,
        calendar[Calendar.DAY_OF_MONTH]
    )
}
