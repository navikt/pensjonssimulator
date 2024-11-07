package no.nav.pensjon.simulator.vedtak.client.pen.acl

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

/**
 * Corresponds 1-to-1 with VedtakResultV1 in PEN
 */
data class PenVedtakResultV1(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val dato: LocalDate?
)
