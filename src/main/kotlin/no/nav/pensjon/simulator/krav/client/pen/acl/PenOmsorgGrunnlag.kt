package no.nav.pensjon.simulator.krav.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.enum.OmsorgTypeEnum

/**
 * Omsorgsgrunnlag DTO (data transfer object) received from PEN.
 * Corresponds to no.nav.pensjon.pen.domain.api.simulator.grunnlag.Omsorgsgrunnlag in PEN.
 */
class PenOmsorgGrunnlag(
    var ar: Int = 0,
    var omsorgTypeEnum: OmsorgTypeEnum? = null,
    var personOmsorgFor: PenPenPerson? = null,
    var bruk: Boolean = false
)
