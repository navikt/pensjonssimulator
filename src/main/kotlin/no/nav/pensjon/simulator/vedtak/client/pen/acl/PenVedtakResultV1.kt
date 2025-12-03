package no.nav.pensjon.simulator.vedtak.client.pen.acl

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import java.time.LocalDate

/**
 * Corresponds 1-to-1 with VedtakResultV1 in PEN
 */
data class PenVedtakResultV1(
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val dato: LocalDate?
)
