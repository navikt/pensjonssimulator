package no.nav.pensjon.simulator.krav.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.KravlinjeTypeCti
import no.nav.pensjon.simulator.core.krav.KravlinjeStatus

/**
 * Kravlinje DTO (data transfer object) received from PEN.
 * Corresponds to no.nav.pensjon.pen.domain.api.simulator.krav.Kravlinje in PEN.
 */
class PenKravlinje {
    var kravlinjeStatus: KravlinjeStatus? = null // PEN: KravlinjeStatusCode
    var land: LandkodeEnum? = null // PEN: Land3TegnCode
    var kravlinjeType: KravlinjeTypeCti? = null
    var kravlinjeTypeEnum: KravlinjeTypeEnum? = null
    var relatertPerson: PenPenPerson? = null

    fun distinguisher(): String =
        (kravlinjeStatus?.name ?: "?") +
                (kravlinjeType?.kode ?: "?") +
                (kravlinjeTypeEnum?.name ?: "?") +
                (land?.name ?: "?") +
                (relatertPerson?.penPersonId ?: "?")
}
