package no.nav.pensjon.simulator.core.domain.regler

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpHistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.GenerellHistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uforehistorikk
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

// Aligned with pensjon-regler-api 2026-01-16
class PenPerson(var penPersonId: Long = 0) {

    //--- Extra:
    @JsonIgnore var pid: Pid? = null
    @JsonIgnore var foedselsdato: LocalDate? = null
    @JsonIgnore var afpHistorikkListe: MutableList<AfpHistorikk>? = null
    @JsonIgnore var uforehistorikk: Uforehistorikk? = null
    @JsonIgnore var generellHistorikk: GenerellHistorikk? = null

    override fun toString() = penPersonId.toString()
    //--- end of extra ---
}
