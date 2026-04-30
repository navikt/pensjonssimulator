package no.nav.pensjon.simulator.krav.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import java.time.LocalDate

/**
 * 'Første virkningsdato-grunnlag' DTO (data transfer object) received from PEN.
 * Corresponds to no.nav.pensjon.pen.domain.api.simulator.grunnlag.ForsteVirkningsdatoGrunnlag in PEN.
 */
class PenFoersteVirkningDatoGrunnlag {
    var virkningsdatoLd: LocalDate? = null
    var kravFremsattDatoLd: LocalDate? = null
    var bruker: PenPenPerson? = null
    var annenPerson: PenPenPerson? = null
    var kravlinjeTypeEnum: KravlinjeTypeEnum? = null
}
