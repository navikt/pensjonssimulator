package no.nav.pensjon.simulator.ytelse.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteBeregning
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.time.LocalDate

data class PenLoependeYtelserResult(
    val alderspensjon: PenAlderspensjonYtelser?,
    val afpPrivat: PenPrivatAfpYtelser?
)

data class PenAlderspensjonYtelser(
    val sokerVirkningFom: LocalDate?,
    val sisteBeregning: SisteBeregning?,
    val forrigeBeregningsresultat: AbstraktBeregningsResultat?,
    val forrigeVilkarsvedtakListe: List<VilkarsVedtak>?,
    val avdoed: PenInformasjonOmAvdoed?
)

data class PenPrivatAfpYtelser(
    val virkningFom: LocalDate? = null,
    val forrigeBeregningsresultat: AbstraktBeregningsResultat? = null
)

// PEN: LoependeYtelserAvdoedResult
data class PenInformasjonOmAvdoed(
    val pid: String?,
    val doedsdato: LocalDate?,
    val foersteVirkningsdato: LocalDate? = null,
    val aarligPensjonsgivendeInntektErMinst1G: Boolean? = null,
    val harTilstrekkeligMedlemskapIFolketrygden: Boolean? = null,
    val antallAarUtenlands: Int? = null,
    val erFlyktning: Boolean? = null
)
