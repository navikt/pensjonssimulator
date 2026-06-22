package no.nav.pensjon.simulator.core

import com.github.benmanes.caffeine.cache.Cache
import no.nav.pensjon.simulator.core.SimulatorContextUtil.finishOpptjeningInit
import no.nav.pensjon.simulator.core.SimulatorContextUtil.personOpptjeningsgrunnlag
import no.nav.pensjon.simulator.core.SimulatorContextUtil.postprocess
import no.nav.pensjon.simulator.core.SimulatorContextUtil.preprocess
import no.nav.pensjon.simulator.core.SimulatorContextUtil.tidsbegrensedeBeholdninger
import no.nav.pensjon.simulator.core.SimulatorContextUtil.updatePersonOpptjeningsFieldFromReglerResponse
import no.nav.pensjon.simulator.core.SimulatorContextUtil.validerOgFerdigstillResponse
import no.nav.pensjon.simulator.core.SimulatorContextUtil.validerResponse
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2011
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2016
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2025
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.core.domain.regler.to.*
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.regel.client.GenericRegelClient
import no.nav.pensjon.simulator.regel.client.RegelClient
import no.nav.pensjon.simulator.tech.cache.CacheConfigurator.createCache
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class SimulatorContext(
    private val regelService: GenericRegelClient,
    cacheManager: CaffeineCacheManager
) : RegelClient {

    private val grunnbeloepCache: Cache<LocalDate, SatsResponse> = createCache("grunnbeloep", cacheManager)

    // PEN: BeregnAlderspensjon2011ForsteUttakConsumerCommand.execute
    override fun beregnAlderspensjon2011FoersteUttak(
        spec: BeregnAlderspensjon2011ForsteUttakRequest,
        sakId: Long?
    ): BeregningsResultatAlderspensjon2011 {
        val response: BeregnAlderspensjon2011ForsteUttakResponse =
            regelService.makeRegelCall(
                request = spec,
                responseClass = BeregnAlderspensjon2011ForsteUttakResponse::class.java,
                serviceName = "beregnAlderspensjon2011ForsteUttak"
            )

        validerResponse(response.pakkseddel, spec, "beregnAlderspensjon2011FoersteUttak")

        return response.beregningsResultat?.apply {
            virkTomLd = null
            beregningsinformasjon = null
        } ?: throw RuntimeException("No beregningsResultat from beregnAlderspensjon2011ForsteUttak")
    }

    // PEN: BeregnAlderspensjon2016ForsteUttakConsumerCommand.execute
    override fun beregnAlderspensjon2016FoersteUttak(
        spec: BeregnAlderspensjon2016ForsteUttakRequest,
        sakId: Long?
    ): BeregningsResultatAlderspensjon2016 {
        val response: BeregnAlderspensjon2016ForsteUttakResponse =
            regelService.makeRegelCall(
                request = spec,
                responseClass = BeregnAlderspensjon2016ForsteUttakResponse::class.java,
                serviceName = "beregnAlderspensjon2016ForsteUttak"
            )

        validerResponse(response.pakkseddel, spec, "beregnAlderspensjon2016FoersteUttak")

        return response.beregningsResultat?.apply {
            virkTomLd = null
            beregningsinformasjon = null
        } ?: throw RuntimeException("No beregningsResultat from beregnAlderspensjon2016ForsteUttak")
    }

    // PEN: BeregnAlderspensjon2025ForsteUttakConsumerCommand.execute
    override fun beregnAlderspensjon2025FoersteUttak(
        spec: BeregnAlderspensjon2025ForsteUttakRequest,
        sakId: Long?
    ): BeregningsResultatAlderspensjon2025 {
        val response: BeregnAlderspensjon2025ForsteUttakResponse =
            regelService.makeRegelCall(
                request = spec,
                responseClass = BeregnAlderspensjon2025ForsteUttakResponse::class.java,
                serviceName = "beregnAlderspensjon2025ForsteUttak"
            )

        validerResponse(response.pakkseddel, spec, "beregnAlderspensjon2025FoersteUttak")

        return response.beregningsResultat?.apply {
            virkTomLd = null
            beregningsinformasjon = null
        } ?: throw RuntimeException("No beregningsResultat from beregnAlderspensjon2025ForsteUttak")
    }

    override fun beregnPoengtallBatch(
        opptjeningGrunnlagListe: MutableList<Opptjeningsgrunnlag>,
        foedselsdato: LocalDate?
    ): MutableList<Opptjeningsgrunnlag> {
        val personOpptjeningsgrunnlagList = opptjeningGrunnlagListe.map { personOpptjeningsgrunnlag(it, foedselsdato) }
        // this call updates all opptjeningsgrunnlag - no need to return them, because the caller has the opptjeningsgrunnlag reference.
        beregnPoengtallBatch(personOpptjeningsgrunnlagList)
        // ..but in order to adhere to the service design: TODO
        return opptjeningGrunnlagListe
    }

    // PEN: SimulatorContext.beregnPoengtallBatch
    private fun beregnPoengtallBatch(inputList: List<PersonOpptjeningsgrunnlag>): List<PersonOpptjeningsgrunnlag> {
        val response: BeregnPoengtallBatchResponse = regelService.makeRegelCall(
            request = BeregnPoengtallBatchRequest().apply {
                personOpptjeningsgrunnlagListe = inputList.toMutableList()
            },
            responseClass = BeregnPoengtallBatchResponse::class.java,
            serviceName = "beregnPoengtallBatch"
        )

        val outputList = response.personOpptjeningsgrunnlagListe
        updatePersonOpptjeningsFieldFromReglerResponse(inputList, outputList)
        return outputList
    }

    override fun revurderAlderspensjon2011(
        spec: RevurderingAlderspensjon2011Request,
        sakId: Long?
    ): BeregningsResultatAlderspensjon2011 {
        val response: RevurderingAlderspensjon2011Response =
            regelService.makeRegelCall(
                request = spec,
                responseClass = RevurderingAlderspensjon2011Response::class.java,
                serviceName = "revurderingAlderspensjon2011"
            )

        validerResponse(response.pakkseddel, spec, "revurderingAlderspensjon2011")
        return response.revurdertBeregningsResultat!!
    }

    override fun revurderAlderspensjon2016(
        spec: RevurderingAlderspensjon2016Request,
        sakId: Long?
    ): BeregningsResultatAlderspensjon2016 {
        val response: RevurderingAlderspensjon2016Response =
            regelService.makeRegelCall(
                request = spec,
                responseClass = RevurderingAlderspensjon2016Response::class.java,
                serviceName = "revurderingAlderspensjon2016"
            )

        validerResponse(response.pakkseddel, spec, "revurderingAlderspensjon2016")
        return response.revurdertBeregningsResultat!!
    }

    override fun revurderAlderspensjon2025(
        spec: RevurderingAlderspensjon2025Request,
        sakId: Long?
    ): BeregningsResultatAlderspensjon2025 {
        val response: RevurderingAlderspensjon2025Response =
            regelService.makeRegelCall(
                request = spec,
                responseClass = RevurderingAlderspensjon2025Response::class.java,
                serviceName = "revurderingAlderspensjon2025"
            )

        validerResponse(response.pakkseddel, spec, "revurderingAlderspensjon2025")
        return response.revurdertBeregningsResultat!!
    }

    // PEN: SimulerPensjonsberegningConsumerCommand.kallSimuleringsTjeneste
    override fun simulerPensjon(spec: SimuleringRequest, serviceName: String): Simuleringsresultat {
        val response: SimuleringResponse =
            regelService.makeRegelCall(
                request = spec,
                responseClass = SimuleringResponse::class.java,
                serviceName
            )

        return validerOgFerdigstillResponse(response) ?: throw RuntimeException("Simuleringsresultat is null")
    }

    // PEN: SimulerPensjonsberegningConsumerCommand.execute for AFP (pre-2025 offentlig AFP)
    override fun simulerPre2025OffentligAfp(spec: SimuleringRequest): Simuleringsresultat {
        val response: SimuleringResponse =
            regelService.makeRegelCall(
                request = spec,
                responseClass = SimuleringResponse::class.java,
                serviceName = "simulerAFP"
            )

        validerResponse(response.pakkseddel)
        return response.simuleringsResultat ?: throw RuntimeException("Simuleringsresultat is null")
    }

    // PEN: SimulerVilkarsprovAfpConsumerCommand.execute (tidsbegrenset offentlig AFP)
    override fun simulerVilkarsprovPre2025OffentligAfp(spec: SimuleringRequest): Simuleringsresultat {
        val response: SimuleringResponse =
            regelService.makeRegelCall(
                request = spec,
                responseClass = SimuleringResponse::class.java,
                serviceName = "simulerVilkarsprovAFP"
            )

        validerResponse(response.pakkseddel)
        return response.simuleringsResultat ?: throw RuntimeException("Simuleringsresultat is null")
    }


    // PEN: VilkarsprovAlderspensjonOver67ConsumerCommand.execute
    override fun vilkaarsproevUbetingetAlderspensjon(
        spec: VilkarsprovRequest,
        sakId: Long?
    ): MutableList<VilkarsVedtak> {
        val response: VilkarsprovResponse =
            regelService.makeRegelCall(
                request = spec,
                responseClass = VilkarsprovResponse::class.java,
                serviceName = "vilkarsprovAlderspensjonOver67"
            )

        validerResponse(response.pakkseddel, spec, "vilkaarsproevUbetingetAlderspensjon")
        return response.vedtaksliste
    }

    // PEN: VilkarsprovAlderspensjon2011ConsumerCommand.execute
    override fun vilkaarsproevAlderspensjon2011(
        spec: VilkarsprovAlderpensjon2011Request,
        sakId: Long?
    ): MutableList<VilkarsVedtak> {
        preprocess(spec)

        val response: VilkarsprovResponse = regelService.makeRegelCall(
            request = spec,
            responseClass = VilkarsprovResponse::class.java,
            serviceName = "vilkarsprovAlderspensjon2011"
        )

        validerResponse(response.pakkseddel, spec, "vilkarsprovAlderspensjon2011")
        return response.vedtaksliste
    }

    // PEN: VilkarsprovAlderspensjon2016ConsumerCommand.execute
    override fun vilkaarsproevAlderspensjon2016(
        spec: VilkarsprovAlderpensjon2016Request,
        sakId: Long?
    ): MutableList<VilkarsVedtak> {
        preprocess(spec)

        val response: VilkarsprovResponse = regelService.makeRegelCall(
            request = spec,
            responseClass = VilkarsprovResponse::class.java,
            serviceName = "vilkarsprovAlderspensjon2016"
        )

        validerResponse(response.pakkseddel, spec, "vilkarsprovAlderspensjon2016")
        return response.vedtaksliste
    }

    // PEN: VilkarsprovAlderspensjon2025ConsumerCommand.execute
    override fun vilkaarsproevAlderspensjon2025(
        spec: VilkarsprovAlderpensjon2025Request,
        sakId: Long?
    ): MutableList<VilkarsVedtak> {
        preprocess(spec)

        val response: VilkarsprovResponse = regelService.makeRegelCall(
            request = spec,
            responseClass = VilkarsprovResponse::class.java,
            serviceName = "vilkarsprovAlderspensjon2025"
        )

        validerResponse(response.pakkseddel, spec, "vilkaarsproevAlderspensjon2025")
        return response.vedtaksliste
    }

    // PEN: BeregnAFPpaslagPrivatConsumerCommand.execute
    //@Throws(PEN165KanIkkeBeregnesException::class, PEN166BeregningsmotorValidereException::class)
    override fun beregnPrivatAfp(spec: BeregnAfpPrivatRequest, sakId: Long?): BeregningsResultatAfpPrivat {
        val response: BeregnAfpPrivatResponse = regelService.makeRegelCall(
            request = spec,
            responseClass = BeregnAfpPrivatResponse::class.java,
            serviceName = "beregnAfpPrivat"
        )

        validerResponse(response.pakkseddel)
        response.beregningsResultatAfpPrivat?.let(::postprocess)
        return response.beregningsResultatAfpPrivat!!
    }

    // PEN: FastsettTrygdetidConsumerCommand.execute
    override fun refreshFastsettTrygdetid(
        spec: TrygdetidRequest,
        kravIsUfoeretrygd: Boolean,
        sakId: Long?
    ): TrygdetidResponse {
        val response: TrygdetidResponse = regelService.makeRegelCall(
            request = spec,
            responseClass = TrygdetidResponse::class.java,
            serviceName = "fastsettTrygdetid"
        )

        validerOgFerdigstillResponse(response, kravIsUfoeretrygd, spec, "fastsettTrygdetid")
        return response
    }

    // PEN: DefaultBeregningConsumerService.beregnOpptjening -> BeregnOpptjeningConsumerCommand.execute
    // NB: No sakId in legacy code
    override fun beregnOpptjening(
        beholdningTom: LocalDate?,
        persongrunnlag: Persongrunnlag,
        beholdning: Pensjonsbeholdning?
    ): MutableList<Pensjonsbeholdning> {
        persongrunnlag.personDetaljListe.forEach(::setRollePeriode)

        val request = BeregnPensjonsBeholdningRequest().apply {
            this.beholdningTomLd = beholdningTom
            this.persongrunnlag = persongrunnlag
            this.beholdning = beholdning
        }

        val response =
            regelService.makeRegelCall<BeregnPensjonsBeholdningResponse, BeregnPensjonsBeholdningRequest>(
                request = request,
                responseClass = BeregnPensjonsBeholdningResponse::class.java,
                serviceName = "beregnPensjonsBeholdning"
            ).also {
                validerResponse(it.pakkseddel)
                finishOpptjeningInit(it.beholdninger)
            }

        return tidsbegrensedeBeholdninger(response.beholdninger)
    }

    // PEN: no.nav.consumer.pensjon.pen.regler.grunnlag.support.command.HentGrunnbelopListeConsumerCommand.execute
    override fun fetchGrunnbeloepListe(dato: LocalDate): SatsResponse =
        grunnbeloepCache.getIfPresent(dato) ?: fetchFreshGrunnbeloep(dato).also { grunnbeloepCache.put(dato, it) }

    override fun hentDelingstall(request: HentDelingstallRequest): HentDelingstallResponse =
        regelService.makeRegelCall(
            request,
            responseClass = HentDelingstallResponse::class.java,
            serviceName = "delingstall"
        )

    private fun fetchFreshGrunnbeloep(dato: LocalDate): SatsResponse =
        regelService.makeRegelCall(
            request = HentGrunnbelopListeRequest().apply {
                fomLd = dato
                tomLd = dato
            },
            responseClass = SatsResponse::class.java,
            serviceName = "hentGrunnbelopListe"
        )

    // TODO: May be unnecessary, since this is done in PersonDetalj.finishInit
    private fun setRollePeriode(detalj: PersonDetalj) {
        detalj.virkFom?.let { detalj.rolleFomDatoLd = it }
        detalj.virkTom?.let { detalj.rolleTomDatoLd = it }
    }
}