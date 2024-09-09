package no.nav.pensjon.simulator.core.afp.offentlig.pre2025

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.time.LocalDate

/**
 * Vedtak in context of pre-2025 offentlig AFP.
 */
data class Pre2025OffentligAfpVedtak(
    val fom: LocalDate,
    val vilkarsvedtakListe: List<VilkarsVedtak>,
    val beregningResultater: List<AbstraktBeregningsResultat>,
    val kravhode: Kravhode?,
    val isKravOnOldRegelverk: Boolean
)
