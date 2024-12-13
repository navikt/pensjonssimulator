package no.nav.pensjon.simulator.ytelse

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteBeregning
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.time.LocalDate

data class LoependeYtelserResult(
    val alderspensjon: AlderspensjonYtelser?,
    val afpPrivat: PrivatAfpYtelser?
)

data class AlderspensjonYtelser(
    val sokerVirkningFom: LocalDate?,
    val avdodVirkningFom: LocalDate?,
    val sisteBeregning: SisteBeregning?,
    val forrigeBeregningsresultat: AbstraktBeregningsResultat?,
    val forrigeVilkarsvedtakListe: List<VilkarsVedtak>
)

data class PrivatAfpYtelser(
    val virkningFom: LocalDate? = null,
    val forrigeBeregningsresultat: AbstraktBeregningsResultat? = null
)
