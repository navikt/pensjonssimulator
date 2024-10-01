package no.nav.pensjon.simulator.regel.client

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2011
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2016
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2025
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.to.*
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.time.LocalDate

interface RegelClient {

    fun beregnAlderspensjon2011FoersteUttak(
        spec: BeregnAlderspensjon2011ForsteUttakRequest,
        sakId: Long?
    ): BeregningsResultatAlderspensjon2011

    fun beregnAlderspensjon2016FoersteUttak(
        spec: BeregnAlderspensjon2016ForsteUttakRequest,
        sakId: Long?
    ): BeregningsResultatAlderspensjon2016

    fun beregnAlderspensjon2025FoersteUttak(
        spec: BeregnAlderspensjon2025ForsteUttakRequest,
        sakId: Long?
    ): BeregningsResultatAlderspensjon2025

    fun beregnPoengtallBatch(
        opptjeningGrunnlagListe: MutableList<Opptjeningsgrunnlag>,
        foedselDato: LocalDate?
    ): MutableList<Opptjeningsgrunnlag>

    fun revurderAlderspensjon2011(
        spec: RevurderingAlderspensjon2011Request,
        sakId: Long?
    ): BeregningsResultatAlderspensjon2011

    fun revurderAlderspensjon2016(
        spec: RevurderingAlderspensjon2016Request,
        sakId: Long?
    ): BeregningsResultatAlderspensjon2016

    fun revurderAlderspensjon2025(
        spec: RevurderingAlderspensjon2025Request,
        sakId: Long?
    ): BeregningsResultatAlderspensjon2025

    fun vilkaarsproevUbetingetAlderspensjon(
        spec: VilkarsprovRequest,
        sakId: Long?
    ): MutableList<VilkarsVedtak>

    fun vilkaarsproevAlderspensjon2011(
        spec: VilkarsprovAlderpensjon2011Request,
        sakId: Long?
    ): MutableList<VilkarsVedtak>

    fun vilkaarsproevAlderspensjon2016(
        spec: VilkarsprovAlderpensjon2016Request,
        sakId: Long?
    ): MutableList<VilkarsVedtak>

    fun vilkaarsproevAlderspensjon2025(
        spec: VilkarsprovAlderpensjon2025Request,
        sakId: Long?
    ): MutableList<VilkarsVedtak>

    fun beregnPrivatAfp(
        spec: BeregnAfpPrivatRequest,
        sakId: Long?
    ): BeregningsResultatAfpPrivat

    fun refreshFastsettTrygdetid(
        spec: TrygdetidRequest,
        kravIsUfoeretrygd: Boolean,
        sakId: Long?
    ): TrygdetidResponse

    fun beregnOpptjening(
        beholdningTom: LocalDate?,
        persongrunnlag: Persongrunnlag,
        beholdning: Pensjonsbeholdning? = null
    ): MutableList<Pensjonsbeholdning>

    fun fetchGrunnbeloepListe(localDate: LocalDate): SatsResponse

    fun fetchGyldigSats(request: HentGyldigSatsRequest): SatsResponse

    fun regulerPensjonsbeholdning(request: RegulerPensjonsbeholdningRequest): RegulerPensjonsbeholdningResponse
}
