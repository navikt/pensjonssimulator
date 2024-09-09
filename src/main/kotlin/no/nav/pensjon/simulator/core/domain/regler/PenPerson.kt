package no.nav.pensjon.simulator.core.domain.regler

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpHistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.GenerellHistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uforehistorikk
import no.nav.pensjon.simulator.person.Pid
import java.io.Serializable
import java.util.*

class PenPerson(var penPersonId: Long = 0) : Serializable {

    @JsonIgnore var pid: Pid? = null
    @JsonIgnore var fodselsdato: Date? = null
    @JsonIgnore var afpHistorikkListe: MutableList<AfpHistorikk>? = null
    @JsonIgnore var uforehistorikk: Uforehistorikk? = null
    @JsonIgnore var generellHistorikk: GenerellHistorikk? = null

    constructor(penPerson: PenPerson) : this() {
        this.penPersonId = penPerson.penPersonId
    }

    override fun toString() = penPersonId.toString()
}
