package no.nav.pensjon.simulator.core.afp.privat

import no.nav.pensjon.simulator.core.SimuleringSpec
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import java.time.LocalDate

// no.nav.service.pensjon.simulering.abstractsimulerapfra2011.BeregnAfpPrivatRequest
data class PrivatAfpSpec(
    val simulering: SimuleringSpec,
    val kravhode: Kravhode,
    val virkningFom: LocalDate?,
    val forrigePrivatAfpBeregningResult: BeregningsResultatAfpPrivat?,
    val gjelderOmsorg: Boolean,
    val sakId: Long?
)
