package no.nav.pensjon.simulator.afp.privat

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import java.time.LocalDate

/**
 * PEN: no.nav.service.pensjon.simulering.abstractsimulerapfra2011.BeregnAfpPrivatRequest
 */
data class PrivatAfpSpec(
    val kravhode: Kravhode,
    val virkningFom: LocalDate,
    val foersteUttakDato: LocalDate?,
    val forrigePrivatAfpBeregningResult: BeregningsResultatAfpPrivat?,
    val gjelderOmsorg: Boolean,
    val sakId: Long?
)
