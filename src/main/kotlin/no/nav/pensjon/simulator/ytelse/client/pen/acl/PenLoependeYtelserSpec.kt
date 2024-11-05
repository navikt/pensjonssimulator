package no.nav.pensjon.simulator.ytelse.client.pen.acl

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

/**
 * Corresponds 1-to-1 with LoependeYtelserSpecV1 in PEN.
 */
data class PenLoependeYtelserSpec(
    val pid: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val foersteUttakDato: LocalDate,
    val avdoed: PenAvdoedYtelserSpec?,
    val alderspensjonFlags: PenAlderspensjonYtelserFlags?,
    val pre2025OffentligAfpYtelserFlags: PenPre2025OffentligAfpYtelserFlags?
)

/**
 * Corresponds 1-to-1 with AvdoedYtelserSpecV1 in PEN.
 */
data class PenAvdoedYtelserSpec(
    val pid: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val doedDato: LocalDate?
)

/**
 * Corresponds 1-to-1 with AlderspensjonYtelserFlagsV1 in PEN.
 */
data class PenAlderspensjonYtelserFlags(
    val inkluderPrivatAfp: Boolean
)

/**
 * Corresponds 1-to-1 with Pre2025OffentligAfpYtelserFlagsV1 in PEN.
 */
data class PenPre2025OffentligAfpYtelserFlags(
    val gjelderFpp: Boolean,
    val sivilstatusUdefinert: Boolean
)
