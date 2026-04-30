package no.nav.pensjon.simulator.ytelse.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsvilkarPeriode
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteBeregning
import no.nav.pensjon.simulator.core.domain.regler.enum.BegrunnelseTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.VedtakResultatEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.VilkarVurderingEnum
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.domain.regler.vedtak.AbstraktVilkarsprovResultat
import java.time.LocalDate
import java.util.Date

data class PenLoependeYtelserResult(
    val alderspensjon: PenAlderspensjonYtelser?,
    val afpPrivat: PenPrivatAfpYtelser?
)

data class PenAlderspensjonYtelser(
    val sokerVirkningFom: LocalDate?,
    val sisteBeregning: SisteBeregning?,
    val forrigeBeregningsresultat: AbstraktBeregningsResultat?,
    val forrigeVilkarsvedtakListe: List<PenVilkaarsvedtak>?,
    val avdoed: PenInformasjonOmAvdoed?
)

data class PenPrivatAfpYtelser(
    val virkningFom: LocalDate? = null,
    val forrigeBeregningsresultat: AbstraktBeregningsResultat? = null
)

class PenVilkaarsvedtak {
    var anbefaltResultatEnum: VedtakResultatEnum? = null
    var vilkarsvedtakResultatEnum: VedtakResultatEnum? = null
    var kravlinjeTypeEnum: KravlinjeTypeEnum? = null
    var anvendtVurderingEnum: VilkarVurderingEnum? = null
    var virkFomLd: LocalDate? = null
    var virkTomLd: LocalDate? = null
    var forsteVirkLd: LocalDate? = null
    var kravlinjeForsteVirkLd: LocalDate? = null
    var kravlinje: Kravlinje? = null
    var penPerson: PenPerson? = null
    var vilkarsprovresultat: AbstraktVilkarsprovResultat? = null
    var begrunnelseEnum: BegrunnelseTypeEnum? = null
    var avslattKapittel19 = false
    var avslattGarantipensjon = false
    var vurderSkattefritakET = false
    var unntakHalvMinstepensjon = false
    var epsRettEgenPensjon = false
    var beregningsvilkarPeriodeListe: List<BeregningsvilkarPeriode> = mutableListOf()
    var merknadListe: List<Merknad> = mutableListOf()
}

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
