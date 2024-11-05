package no.nav.pensjon.simulator.ytelse.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteBeregning
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.time.LocalDate

data class PenLoependeYtelserResult(
    val alderspensjon: PenAlderspensjonYtelser,
    val afpPrivat: PenPrivatAfpYtelser
)

data class PenAlderspensjonYtelser(
    val sokerVirkningFom: LocalDate?,
    val avdodVirkningFom: LocalDate?,
    val sisteBeregning: SisteBeregning?,
    val forrigeBeregningsresultat: AbstraktBeregningsResultat?,
    val forrigeVilkarsvedtakListe: List<VilkarsVedtak>
)

data class PenPrivatAfpYtelser(
    val virkningFom: LocalDate? = null,
    val forrigeBeregningsresultat: AbstraktBeregningsResultat? = null
)
