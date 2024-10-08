package no.nav.pensjon.simulator.core.ytelse

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteBeregning
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.time.LocalDate

data class LoependeYtelser(
    val soekerVirkningFom: LocalDate,
    val avdoedVirkningFom: LocalDate?,
    val privatAfpVirkningFom: LocalDate?,
    val sisteBeregning: SisteBeregning?,
    val forrigeAlderspensjonBeregningResultat: AbstraktBeregningsResultat?,
    val forrigePrivatAfpBeregningResultat: AbstraktBeregningsResultat?,
    val forrigeVedtakListe: MutableList<VilkarsVedtak>
)
