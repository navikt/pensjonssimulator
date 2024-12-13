package no.nav.pensjon.simulator.krav.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpHistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.GenerellHistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uforehistorikk
import no.nav.pensjon.simulator.person.Pid
import java.util.Date

/**
 * PEN-person DTO (data transfer object) received from PEN.
 * Corresponds to PenPersonDtoForSimulator in PEN.
 */
class PenPenPerson(var penPersonId: Long = 0) {
    var pid: Pid? = null
    var fodselsdato: Date? = null
    var afpHistorikkListe: MutableList<AfpHistorikk>? = null
    var uforehistorikk: Uforehistorikk? = null
    var generellHistorikk: GenerellHistorikk? = null
}
