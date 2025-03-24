package no.nav.pensjon.simulator.krav.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import java.util.Date

/**
 * 'Første virkningsdato-grunnlag' DTO (data transfer object) received from PEN.
 * Corresponds to no.nav.pensjon.pen.domain.api.simulator.grunnlag.ForsteVirkningsdatoGrunnlag in PEN.
 */
class PenFoersteVirkningDatoGrunnlag {
    var virkningsdato: Date? = null
    var kravFremsattDato: Date? = null
    var bruker: PenPenPerson? = null
    var annenPerson: PenPenPerson? = null
    var kravlinjeTypeEnum: KravlinjeTypeEnum? = null
    //--- Extra:
    //var sakType: SakTypeEnum? = null
}
