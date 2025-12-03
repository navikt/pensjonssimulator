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
import no.nav.pensjon.simulator.core.util.DateNoonExtension.noon
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.regel.client.GenericRegelClient
import no.nav.pensjon.simulator.regel.client.RegelClient
import no.nav.pensjon.simulator.tech.cache.CacheConfigurator.createCache
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper
import java.time.LocalDate
import java.util.*

@Component
class SimulatorContext(
    private val regelService: GenericRegelClient,
    private val objectMapper: JsonMapper,
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
                serviceName = "beregnAlderspensjon2011ForsteUttak",
                map = null,
                sakId = sakId?.toString()
            )

        validerResponse(response.pakkseddel, spec, objectMapper, "beregnAlderspensjon2011FoersteUttak")

        return response.beregningsResultat?.apply {
            virkTom = null
            epsMottarPensjon = false
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
                serviceName = "beregnAlderspensjon2016ForsteUttak",
                map = null,
                sakId = sakId?.toString()
            )

        validerResponse(response.pakkseddel, spec, objectMapper, "beregnAlderspensjon2016FoersteUttak")

        return response.beregningsResultat?.apply {
            virkTom = null
            epsMottarPensjon = false
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
                serviceName = "beregnAlderspensjon2025ForsteUttak",
                map = null,
                sakId = sakId?.toString()
            )

        validerResponse(response.pakkseddel, spec, objectMapper, "beregnAlderspensjon2025FoersteUttak")

        return response.beregningsResultat?.apply {
            virkTom = null
            epsMottarPensjon = false
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
            serviceName = "beregnPoengtallBatch",
            map = null,
            sakId = null
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
                serviceName = "revurderingAlderspensjon2011",
                map = null,
                sakId = sakId?.toString()
            )

        validerResponse(response.pakkseddel, spec, objectMapper, "revurderingAlderspensjon2011")
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
                serviceName = "revurderingAlderspensjon2016",
                map = null,
                sakId = sakId?.toString()
            )

        validerResponse(response.pakkseddel, spec, objectMapper, "revurderingAlderspensjon2016")
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
                serviceName = "revurderingAlderspensjon2025",
                map = null,
                sakId = sakId?.toString()
            )

        validerResponse(response.pakkseddel, spec, objectMapper, "revurderingAlderspensjon2025")
        return response.revurdertBeregningsResultat!!
    }

    // PEN: SimulerPensjonsberegningConsumerCommand.execute for AFP (pre-2025 offentlig AFP)
    override fun simulerPre2025OffentligAfp(spec: SimuleringRequest): Simuleringsresultat {
        val response: SimuleringResponse =
            regelService.makeRegelCall(
                request = spec,
                responseClass = SimuleringResponse::class.java,
                serviceName = "simulerAFP",
                map = null,
                sakId = null
            )

        validerResponse(response.pakkseddel)
        return response.simuleringsResultat ?: throw RuntimeException("Simuleringsresultat is null")
    }

    // PEN: SimulerVilkarsprovAfpConsumerCommand.execute (pre-2025 offentlig AFP)
    override fun simulerVilkarsprovPre2025OffentligAfp(spec: SimuleringRequest): Simuleringsresultat {
        val response: SimuleringResponse =
            regelService.makeRegelCall(
                request = spec,
                responseClass = SimuleringResponse::class.java,
                serviceName = "simulerVilkarsprovAFP",
                map = null,
                sakId = null
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
                serviceName = "vilkarsprovAlderspensjonOver67",
                map = null,
                sakId = sakId?.toString()
            )

        validerResponse(response.pakkseddel, spec, objectMapper, "vilkaarsproevUbetingetAlderspensjon")
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
            serviceName = "vilkarsprovAlderspensjon2011",
            map = null,
            sakId = sakId?.toString()
        )

        validerResponse(response.pakkseddel, spec, objectMapper, "vilkarsprovAlderspensjon2011")
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
            serviceName = "vilkarsprovAlderspensjon2016",
            map = null,
            sakId = sakId?.toString()
        )

        validerResponse(response.pakkseddel, spec, objectMapper, "vilkarsprovAlderspensjon2016")
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
            serviceName = "vilkarsprovAlderspensjon2025",
            map = null,
            sakId = sakId?.toString()
        )

        validerResponse(response.pakkseddel, spec, objectMapper, "vilkaarsproevAlderspensjon2025")
        return response.vedtaksliste
    }

    // PEN: BeregnAFPpaslagPrivatConsumerCommand.execute
    //@Throws(PEN165KanIkkeBeregnesException::class, PEN166BeregningsmotorValidereException::class)
    override fun beregnPrivatAfp(spec: BeregnAfpPrivatRequest, sakId: Long?): BeregningsResultatAfpPrivat {
        val response: BeregnAfpPrivatResponse = regelService.makeRegelCall(
            request = spec,
            responseClass = BeregnAfpPrivatResponse::class.java,
            serviceName = "beregnAfpPrivat",
            map = null,
            sakId = sakId?.toString()
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
            serviceName = "fastsettTrygdetid",
            map = null,
            sakId = sakId?.toString()
        )

        validerOgFerdigstillResponse(response, kravIsUfoeretrygd, spec, objectMapper, "fastsettTrygdetid")
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
            this.beholdningTom = beholdningTom?.toNorwegianDateAtNoon()
            this.persongrunnlag = persongrunnlag
            this.beholdning = beholdning
        }

        val response =
            regelService.makeRegelCall<BeregnPensjonsBeholdningResponse, BeregnPensjonsBeholdningRequest>(
                request = request,
                responseClass = BeregnPensjonsBeholdningResponse::class.java,
                serviceName = "beregnPensjonsBeholdning",
                map = null,
                sakId = null
            ).also {
                validerResponse(it.pakkseddel)
                finishOpptjeningInit(it.beholdninger)
            }

        return tidsbegrensedeBeholdninger(response.beholdninger)
    }

    // PEN: no.nav.consumer.pensjon.pen.regler.grunnlag.support.command.HentGrunnbelopListeConsumerCommand.execute
    override fun fetchGrunnbeloepListe(dato: LocalDate): SatsResponse =
        grunnbeloepCache.getIfPresent(dato) ?: fetchFreshGrunnbeloep(dato).also { grunnbeloepCache.put(dato, it) }

    // PEN: HentGyldigSatsConsumerCommand.execute
    override fun fetchGyldigSats(request: HentGyldigSatsRequest): SatsResponse =
        regelService.makeRegelCall(
            request = request,
            responseClass = SatsResponse::class.java,
            serviceName = "hentGyldigSats",
            map = null,
            sakId = null
        )

    // PEN: RegulerPensjonsbeholdningConsumerCommand.execute
    override fun regulerPensjonsbeholdning(request: RegulerPensjonsbeholdningRequest): RegulerPensjonsbeholdningResponse {
        val response: RegulerPensjonsbeholdningResponse =
            regelService.makeRegelCall(
                request = request,
                responseClass = RegulerPensjonsbeholdningResponse::class.java,
                serviceName = "regulerPensjonsbeholdning",
                map = null,
                sakId = null
            )
        return response //TODO validerOgFerdigstillResponse(response) -> mapRegulerPensjonsbeholdningConsumerResponseToPen
    }

    override fun hentDelingstall(request: HentDelingstallRequest): HentDelingstallResponse {
        val response : HentDelingstallResponse = regelService.makeRegelCall(
            request,
            HentDelingstallResponse::class.java,
            "delingstall",
            null,
            null)
        return response
    }

    // PEN: no.nav.consumer.pensjon.pen.regler.grunnlag.support.command.HentGrunnbelopListeConsumerCommand.execute
    private fun fetchFreshGrunnbeloep(localDate: LocalDate): SatsResponse {
        val date: Date = localDate.toNorwegianDateAtNoon()

        return regelService.makeRegelCall(
            request = HentGrunnbelopListeRequest().apply {
                fom = date
                tom = date
            },
            responseClass = SatsResponse::class.java,
            serviceName = "hentGrunnbelopListe",
            map = null,
            sakId = null
        )
    }

    // TODO: May be unnecessary, since this is done in PersonDetalj.finishInit
    private fun setRollePeriode(detalj: PersonDetalj) {
        detalj.virkFom?.let { detalj.rolleFomDato = it.noon() }
        detalj.virkTom?.let { detalj.rolleTomDato = it.noon() }
    }
}
