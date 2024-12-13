package no.nav.pensjon.simulator.krav.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.kode.OmsorgTypeCti

/**
 * Omsorgsgrunnlag DTO (data transfer object) received from PEN.
 * Corresponds to OmsorgsgrunnlagDtoForSimulator in PEN.
 */
class PenOmsorgGrunnlag(
    var ar: Int = 0,
    var omsorgType: OmsorgTypeCti? = null,
    var personOmsorgFor: PenPenPerson? = null,
    var bruk: Boolean = false
)
