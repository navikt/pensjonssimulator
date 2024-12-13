package no.nav.pensjon.simulator.krav.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.kode.KravlinjeTypeCti
import java.util.Date

/**
 * 'Første virkningsdato-grunnlag' DTO (data transfer object) received from PEN.
 * Corresponds to ForsteVirkningsdatoGrunnlagDtoForSimulator in PEN.
 */
class PenFoersteVirkningDatoGrunnlag {
    var virkningsdato: Date? = null
    var kravFremsattDato: Date? = null
    var bruker: PenPenPerson? = null
    var annenPerson: PenPenPerson? = null
    var kravlinjeType: KravlinjeTypeCti? = null
}
