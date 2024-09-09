package no.nav.pensjon.simulator.core.beholdning

import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

// no.nav.service.pensjon.fpen.HentBeholdningerMedGrunnlagRequest
data class BeholdningerMedGrunnlagSpec(
    val pid: Pid,
    val foedselDato: LocalDate,
    val kravhode: Kravhode,
    val hentPensjonspoeng: Boolean,
    val hentGrunnlagForOpptjeninger: Boolean,
    val hentBeholdninger: Boolean
)
