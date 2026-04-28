package no.nav.pensjon.simulator.person.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpHistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.GenerellHistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uforehistorikk
import java.time.LocalDate

data class PenPersonResult(
    val personerVedPid: Map<String, PenPersonHistorikk>
)

data class PenPersonHistorikk(
    val penPersonId: Long,
    val pid: String?,
    val fodselsdatoLd: LocalDate?,
    val afpHistorikkListe: List<AfpHistorikk>,
    val uforehistorikk: Uforehistorikk?,
    val generellHistorikk: GenerellHistorikk?
)
