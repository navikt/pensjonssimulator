package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3

import com.fasterxml.jackson.annotation.JsonFormat
import java.util.*

data class UtbetalingsperiodeResultV3(
    @field:JsonFormat(shape = JsonFormat.Shape.NUMBER)
    val datoFom: Date? = null,
    @field:JsonFormat(shape = JsonFormat.Shape.NUMBER)
    val datoTom: Date? = null,
    val grad: Int? = null,
    val arligUtbetaling: Double? = null,
    val ytelsekode: String? = null,
    val mangelfullSimuleringkode: String? = null
)