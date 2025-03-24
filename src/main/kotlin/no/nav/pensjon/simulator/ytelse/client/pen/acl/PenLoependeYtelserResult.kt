package no.nav.pensjon.simulator.ytelse.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteBeregning
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.util.*

data class PenLoependeYtelserResultV1(
    val alderspensjon: PenAlderspensjonYtelser?,
    val afpPrivat: PenPrivatAfpYtelser?,
)

data class PenAlderspensjonYtelser(
    val sokerVirkningFom: Date?,
    val avdodVirkningFom: Date?,
    val sisteBeregning: SisteBeregning?,
    val forrigeBeregningsresultat: AbstraktBeregningsResultat?,
    val forrigeVilkarsvedtakListe: List<VilkarsVedtak>?
)

data class PenPrivatAfpYtelser(
    val virkningFom: Date? = null,
    val forrigeBeregningsresultat: AbstraktBeregningsResultat? = null
)
