package no.nav.pensjon.simulator.vedtak.client.pen.acl

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import java.time.LocalDate

data class PenVedtakSpecV1(
    val pid: String,
    val sakType: SakTypeEnum
)

/**
 * Corresponds to VedtakStatusForSimulatorSpecV1 in PEN
 */
data class PenVedtakStatusSpec(
    val pid: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val uttakFom: LocalDate?
)
