package no.nav.pensjon.simulator.core.result

import java.time.LocalDate

/**
 * Data fra Nav-registre som skal inkluderes som informasjon i resultatet.
 */
data class RegisterData(
    val sisteLignetInntektAar: Int? = null,
    val sisteGyldigeOpptjeningAar: Int? = null,
    val grunnbeloep: Int? = null,
    val soekerFoedselsdato: LocalDate? = null
)