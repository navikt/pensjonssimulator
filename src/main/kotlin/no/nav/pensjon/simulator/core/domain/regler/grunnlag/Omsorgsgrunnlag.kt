package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.OmsorgTypeEnum
import no.nav.pensjon.simulator.person.Pid

// Copied from pensjon-regler-api 2026-01-16
class Omsorgsgrunnlag {
    var ar = 0
    var omsorgTypeEnum: OmsorgTypeEnum? = null
    var personOmsorgFor: PenPerson? = null
    var bruk = false

    //--- Extra:
    @JsonIgnore
    var pidOmsorgFor: Pid? = null
}