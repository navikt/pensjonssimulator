package no.nav.pensjon.simulator.core.domain

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.time.LocalDate

/**
 * Corresponds to no.nav.service.pensjon.simulering.ap2025.endring.SimulatorVedtak in PEN
 */
data class Vedtak(
    val fom: LocalDate,
    val vilkaarVedtakListe: List<VilkarsVedtak>,
    val beregningResultatListe: List<AbstraktBeregningsResultat>,
    val kravhode: Kravhode?,
    val erKravPaaGammeltRegelverk: Boolean
)
