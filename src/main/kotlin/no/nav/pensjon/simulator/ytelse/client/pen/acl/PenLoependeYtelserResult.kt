package no.nav.pensjon.simulator.ytelse.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteBeregning
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.util.*

data class PenLoependeYtelserResultV1(
    val ytelser: PenLoependeYtelserResult,
    val extraAlderspensjonInfo: PenYtelserExtraInfo,
    val extraPrivatAfpInfo: PenYtelserExtraInfo
)

/**
 * Conveys information annotated by @JsonIgnore in pensjon-regler classes
 * (and hence not included in those classes in the response).
 */
data class PenYtelserExtraInfo(
    val kravId: Long? = null,
    val virkTom: Date? = null,
    val epsOver2G: Boolean = false,
    val epsMottarPensjon: Boolean = false,
    val beregningInformasjon: BeregningsInformasjon? = null
)

data class PenLoependeYtelserResult(
    val alderspensjon: PenAlderspensjonYtelser?,
    val afpPrivat: PenPrivatAfpYtelser?
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
