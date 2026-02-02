package no.nav.pensjon.simulator.uttak

import no.nav.pensjon.simulator.validity.Problem
import java.time.LocalDate

data class TidligstMuligUttak(
    val uttaksdato: LocalDate? = null,
    val uttaksgrad: Uttaksgrad,
    val problem: Problem? = null
)
