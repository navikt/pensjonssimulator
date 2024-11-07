package no.nav.pensjon.simulator.vedtak.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum

data class PenVedtakSpecV1(
    val pid: String,
    val sakType: SakTypeEnum
)
