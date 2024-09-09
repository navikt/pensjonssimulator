package no.nav.pensjon.simulator.core

import no.nav.pensjon.simulator.core.afp.privat.PrivatAfpSatser
import no.nav.pensjon.simulator.core.domain.regler.Pakkseddel
import no.nav.pensjon.simulator.core.domain.regler.VeietSatsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2011
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2016
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2025
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.regler.to.*
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.exception.BeregningsmotorValidereException
import no.nav.pensjon.simulator.core.exception.KanIkkeBeregnesException
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.createDate
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.fromLocalDate
import no.nav.pensjon.simulator.core.util.DateNoonExtension.noon
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.regler.PensjonReglerConsumerService
import org.apache.commons.lang3.NotImplementedException
import java.math.RoundingMode
import java.time.LocalDate
import java.util.*

class SimulatorContext(
    private val pensjonReglerService: PensjonReglerConsumerService
) {
    // Requires PDL service
    fun fetchFoedselDato(pid: Pid): LocalDate = throw NotImplementedException()

    // Requires PEN service
    // hentSatserAfpPrivat
    fun fetchPrivatAfpSatser(virkningFom: LocalDate?, foedselDato: LocalDate?): PrivatAfpSatser {
        return PrivatAfpSatser(ft = null) //TODO
    }

    // Requires PEN service
    // hentDelingstallUtvalg
    fun fetchDelingstallUtvalg(virkningFom: LocalDate, foedselDato: LocalDate): DelingstallUtvalg {
        return DelingstallUtvalg() //TODO
    }

    // Requires PEN service
    // hentForholdstallUtvalg
    fun fetchForholdstallUtvalg(virkningFom: LocalDate, foedselDato: LocalDate): ForholdstallUtvalg {
        return ForholdstallUtvalg() //TODO
    }

    // Requires PEN service
    // hentVeietGrunnbelopListe
    fun fetchVeietGrunnbeloepListe(fomYear: Int?, tomYear: Int?): List<VeietSatsResultat> {
        return emptyList() //TODO
    }

    // BeregnAlderspensjon2011ForsteUttakConsumerCommand.execute
    fun beregnAlderspensjon2011FoersteUttak(
        spec: BeregnAlderspensjon2011ForsteUttakRequest,
        sakId: Long?
    ): BeregningsResultatAlderspensjon2011 {
        val response: BeregnAlderspensjon2011ForsteUttakResponse =
            pensjonReglerService.regelServiceApi(
                regelRequest = spec,
                responseClass = BeregnAlderspensjon2011ForsteUttakResponse::class.java,
                serviceName = "beregnAlderspensjon2011ForsteUttak",
                map = null,
                sakId = sakId?.toString()
            )

        validerResponse(response.pakkseddel)

        return response.beregningsResultat?.apply {
            virkTom = null
            epsMottarPensjon = false
        } ?: throw RuntimeException("No beregningsResultat from beregnAlderspensjon2011ForsteUttak")
    }

    // BeregnAlderspensjon2016ForsteUttakConsumerCommand.execute
    fun beregnAlderspensjon2016FoersteUttak(
        spec: BeregnAlderspensjon2016ForsteUttakRequest,
        sakId: Long?
    ): BeregningsResultatAlderspensjon2016 {
        val response: BeregnAlderspensjon2016ForsteUttakResponse =
            pensjonReglerService.regelServiceApi(
                regelRequest = spec,
                responseClass = BeregnAlderspensjon2016ForsteUttakResponse::class.java,
                serviceName = "beregnAlderspensjon2016ForsteUttak",
                map = null,
                sakId = sakId?.toString()
            )

        validerResponse(response.pakkseddel)

        return response.beregningsResultat?.apply {
            virkTom = null
            epsMottarPensjon = false
        } ?: throw RuntimeException("No beregningsResultat from beregnAlderspensjon2016ForsteUttak")
    }

    // BeregnAlderspensjon2025ForsteUttakConsumerCommand.execute
    fun beregnAlderspensjon2025FoersteUttak(
        spec: BeregnAlderspensjon2025ForsteUttakRequest,
        sakId: Long?
    ): BeregningsResultatAlderspensjon2025 {
        val response: BeregnAlderspensjon2025ForsteUttakResponse =
            pensjonReglerService.regelServiceApi(
                regelRequest = spec,
                responseClass = BeregnAlderspensjon2025ForsteUttakResponse::class.java,
                serviceName = "beregnAlderspensjon2025ForsteUttak",
                map = null,
                sakId = sakId?.toString()
            )

        validerResponse(response.pakkseddel)

        return response.beregningsResultat?.apply {
            virkTom = null
            epsMottarPensjon = false
        } ?: throw RuntimeException("No beregningsResultat from beregnAlderspensjon2025ForsteUttak")
    }

    fun beregnPoengtallBatch(
        opptjeningGrunnlagListe: MutableList<Opptjeningsgrunnlag>,
        foedselDato: LocalDate?
    ): MutableList<Opptjeningsgrunnlag> {
        val personOpptjeningsgrunnlagList = opptjeningGrunnlagListe.map { personOpptjeningsgrunnlag(it, foedselDato) }
        // this call updates all opptjeningsgrunnlag - no need to return them, because the caller has the opptjeningsgrunnlag reference.
        beregnPoengtallBatch(personOpptjeningsgrunnlagList)
        // ..but in order to adhere to the service design: TODO
        return opptjeningGrunnlagListe
    }

    private fun beregnPoengtallBatch(inputList: List<PersonOpptjeningsgrunnlag>): List<PersonOpptjeningsgrunnlag> {
        val response: BeregnPoengtallBatchResponse = pensjonReglerService.regelServiceApi(
            BeregnPoengtallBatchRequest(inputList.toMutableList()),
            BeregnPoengtallBatchResponse::class.java,
            "beregnPoengtallBatch",
            null,
            null
        )

        val outputList = response.personOpptjeningsgrunnlagListe
        updatePersonOpptjeningsFieldFromReglerResponse(inputList, outputList)
        return outputList
    }

    fun revurderAlderspensjon2011(
        spec: RevurderingAlderspensjon2011Request,
        sakId: Long?
    ): BeregningsResultatAlderspensjon2011 {
        val response: RevurderingAlderspensjon2011Response =
            pensjonReglerService.regelServiceApi(
                regelRequest = spec,
                responseClass = RevurderingAlderspensjon2011Response::class.java,
                serviceName = "revurderingAlderspensjon2011",
                map = null,
                sakId = sakId?.toString()
            )

        validerResponse(response.pakkseddel)
        return response.revurdertBeregningsResultat!!
    }

    fun revurderAlderspensjon2016(
        spec: RevurderingAlderspensjon2016Request,
        sakId: Long?
    ): BeregningsResultatAlderspensjon2016 {
        val response: RevurderingAlderspensjon2016Response =
            pensjonReglerService.regelServiceApi(
                regelRequest = spec,
                responseClass = RevurderingAlderspensjon2016Response::class.java,
                serviceName = "revurderingAlderspensjon2016",
                map = null,
                sakId = sakId?.toString()
            )

        validerResponse(response.pakkseddel)
        return response.revurdertBeregningsResultat!!
    }

    fun revurderAlderspensjon2025(
        spec: RevurderingAlderspensjon2025Request,
        sakId: Long?
    ): BeregningsResultatAlderspensjon2025 {
        val response: RevurderingAlderspensjon2025Response =
            pensjonReglerService.regelServiceApi(
                regelRequest = spec,
                responseClass = RevurderingAlderspensjon2025Response::class.java,
                serviceName = "revurderingAlderspensjon2025",
                map = null,
                sakId = sakId?.toString()
            )

        validerResponse(response.pakkseddel)
        return response.revurdertBeregningsResultat!!
    }

    // VilkarsprovAlderspensjonOver67ConsumerCommand.execute
    fun vilkaarsproevUbetingetAlderspensjon(spec: VilkarsprovRequest, sakId: Long?): MutableList<VilkarsVedtak> {
        val response: VilkarsprovResponse =
            pensjonReglerService.regelServiceApi(
                regelRequest = spec,
                responseClass = VilkarsprovResponse::class.java,
                serviceName = "vilkarsprovAlderspensjonOver67",
                map = null,
                sakId = sakId?.toString()
            )

        validerResponse(response.pakkseddel)
        return response.vedtaksliste
    }

    /**
     * Replaces VilkarsprovAlderspensjon2011ConsumerCommand.execute
     */
    fun vilkaarsproevAlderspensjon2011(
        spec: VilkarsprovAlderpensjon2011Request,
        sakId: Long?
    ): MutableList<VilkarsVedtak> {
        preprocess(spec)

        val response: VilkarsprovResponse = pensjonReglerService.regelServiceApi(
            regelRequest = spec,
            responseClass = VilkarsprovResponse::class.java,
            serviceName = "vilkarsprovAlderspensjon2011",
            map = null,
            sakId = sakId?.toString()
        )

        validerResponse(response.pakkseddel)
        return response.vedtaksliste
    }

    // VilkarsprovAlderspensjon2016ConsumerCommand.execute
    fun vilkaarsproevAlderspensjon2016(
        spec: VilkarsprovAlderpensjon2016Request,
        sakId: Long?
    ): MutableList<VilkarsVedtak> {
        preprocess(spec)

        val response: VilkarsprovResponse = pensjonReglerService.regelServiceApi(
            regelRequest = spec,
            responseClass = VilkarsprovResponse::class.java,
            serviceName = "vilkarsprovAlderspensjon2016",
            map = null,
            sakId = sakId?.toString()
        )

        validerResponse(response.pakkseddel)
        return response.vedtaksliste
    }

    // VilkarsprovAlderspensjon2025ConsumerCommand.execute
    fun vilkaarsproevAlderspensjon2025(
        spec: VilkarsprovAlderpensjon2025Request,
        sakId: Long?
    ): MutableList<VilkarsVedtak> {
        preprocess(spec)

        val response: VilkarsprovResponse = pensjonReglerService.regelServiceApi(
            regelRequest = spec,
            responseClass = VilkarsprovResponse::class.java,
            serviceName = "vilkarsprovAlderspensjon2025",
            map = null,
            sakId = sakId?.toString()
        )

        validerResponse(response.pakkseddel)
        return response.vedtaksliste
    }

    // BeregnAFPpaslagPrivatConsumerCommand.execute
    //@Throws(PEN165KanIkkeBeregnesException::class, PEN166BeregningsmotorValidereException::class)
    fun beregnPrivatAfp(spec: BeregnAfpPrivatRequest, sakId: Long?): BeregningsResultatAfpPrivat {
        val response: BeregnAfpPrivatResponse = pensjonReglerService.regelServiceApi(
            regelRequest = spec,
            responseClass = BeregnAfpPrivatResponse::class.java,
            serviceName = "beregnAfpPrivat",
            map = null,
            sakId = sakId?.toString()
        )

        validerResponse(response.pakkseddel)
        response.beregningsResultatAfpPrivat?.let(::postprocess)
        return response.beregningsResultatAfpPrivat!!
    }

    // FastsettTrygdetidConsumerCommand.execute
    fun refreshFastsettTrygdetid(
        spec: TrygdetidRequest,
        kravIsUfoeretrygd: Boolean,
        sakId: Long?
    ): TrygdetidResponse {
        val response: TrygdetidResponse = pensjonReglerService.regelServiceApi(
            regelRequest = spec,
            responseClass = TrygdetidResponse::class.java,
            serviceName = "fastsettTrygdetid",
            map = null,
            sakId = sakId?.toString()
        )

        validerOgFerdigstillResponse(response, kravIsUfoeretrygd)
        return response
    }

    // DefaultBeregningConsumerService.beregnOpptjening -> BeregnOpptjeningConsumerCommand.execute
    // NB: No sakId in legacy code
    fun beregnOpptjening(
        beholdningTom: LocalDate?,
        persongrunnlag: Persongrunnlag,
        beholdning: Pensjonsbeholdning? = null
    ): MutableList<Pensjonsbeholdning> {
        persongrunnlag.personDetaljListe.forEach(::setRollePeriode)
        val request = BeregnPensjonsBeholdningRequest(fromLocalDate(beholdningTom)?.noon(), persongrunnlag, beholdning)

        val response = pensjonReglerService.regelServiceApi<BeregnPensjonsBeholdningResponse, BeregnPensjonsBeholdningRequest>(
            regelRequest = request,
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

    // no.nav.consumer.pensjon.pen.regler.grunnlag.support.command.HentGrunnbelopListeConsumerCommand.execute
    fun fetchGrunnbeloepListe(localDate: LocalDate): SatsResponse {
        val date: Date = fromLocalDate(localDate)!!.noon()

        return pensjonReglerService.regelServiceApi(
            regelRequest = HentGrunnbelopListeRequest(date, date),
            responseClass = SatsResponse::class.java,
            serviceName = "hentGrunnbelopListe",
            map = null,
            sakId = null
        )
    }

    // HentGyldigSatsConsumerCommand.execute
    fun fetchGyldigSats(request: HentGyldigSatsRequest): SatsResponse =
        pensjonReglerService.regelServiceApi(
            regelRequest = request,
            responseClass = SatsResponse::class.java,
            serviceName = "hentGyldigSats",
            map = null,
            sakId = null
        )

    // RegulerPensjonsbeholdningConsumerCommand.execute
    fun regulerPensjonsbeholdning(request: RegulerPensjonsbeholdningRequest): RegulerPensjonsbeholdningResponse {
        val response: RegulerPensjonsbeholdningResponse =
            pensjonReglerService.regelServiceApi(
                regelRequest = request,
                responseClass = RegulerPensjonsbeholdningResponse::class.java,
                serviceName = "regulerPensjonsbeholdning",
                map = null,
                sakId = null
            )
        return response //TODO validerOgFerdigstillResponse(response) -> mapRegulerPensjonsbeholdningConsumerResponseToPen
    }

    // TODO: May be unnecessary, since this is done in PersonDetalj.finishInit
    private fun setRollePeriode(detalj: PersonDetalj) {
        detalj.virkFom?.let { detalj.rolleFomDato = it.noon() }
        detalj.virkTom?.let { detalj.rolleTomDato = it.noon() }
    }

    private companion object {
        private fun finishOpptjeningInit(beholdninger: ArrayList<Pensjonsbeholdning>) {
            beholdninger.forEach {
                it.opptjening?.finishInit()
            }
        }

        // SimuleringEtter2011Context.updateBeholdningerWithFomAndTomDate
        private fun tidsbegrensedeBeholdninger(pensjonsbeholdninger: MutableList<Pensjonsbeholdning>): MutableList<Pensjonsbeholdning> {
            pensjonsbeholdninger.forEach {
                it.fom = createDate(it.ar, Calendar.JANUARY, 1)
                it.tom = createDate(it.ar, Calendar.DECEMBER, 31)
            }

            return pensjonsbeholdninger
        }

        //TODO Verify same preprocessing as for AP2025
        private fun preprocess(request: VilkarsprovAlderpensjon2011Request) {
            request.fom = request.fom?.noon()
            request.kravhode!!.uttaksgradListe.forEach { it.fomDato = it.fomDato?.noon() }

            request.afpLivsvarig?.let {
                it.nettoPerAr = it.nettoPerAr.toBigDecimal().setScale(0, RoundingMode.UP).toDouble()
            }
        }

        //TODO Verify same preprocessing as for AP2025
        private fun preprocess(request: VilkarsprovAlderpensjon2016Request) {
            request.virkFom = request.virkFom?.noon()
            request.kravhode!!.uttaksgradListe.forEach { it.fomDato = it.fomDato?.noon() }

            request.afpLivsvarig?.let {
                it.nettoPerAr = it.nettoPerAr.toBigDecimal().setScale(0, RoundingMode.UP).toDouble()
            }
        }

        private fun preprocess(request: VilkarsprovAlderpensjon2025Request) {
            request.fom = request.fom?.noon()
            request.kravhode!!.uttaksgradListe.forEach { it.fomDato = it.fomDato?.noon() }

            request.afpLivsvarig?.let {
                it.nettoPerAr = it.nettoPerAr.toBigDecimal().setScale(0, RoundingMode.UP).toDouble()
            }
        }

        private fun postprocess(resultat: BeregningsResultatAfpPrivat) {
            resultat.virkTom = null

            resultat.afpPrivatBeregning?.afpLivsvarig?.let {
                it.nettoPerAr = it.nettoPerAr.toBigDecimal().setScale(0, RoundingMode.UP).toDouble()
            }
        }

        private fun validerOgFerdigstillResponse(response: TrygdetidResponse, kravIsUforetrygd: Boolean) {
            validerResponse(response.pakkseddel)

            if (kravIsUforetrygd) {
                response.trygdetid?.apply {
                    virkFom = null
                    virkTom = null
                }
            }
        }

        // RegelHelper.validateResponse
        private fun validerResponse(pakkseddel: Pakkseddel) {
            val kontrollTjenesteOk = pakkseddel.kontrollTjenesteOk
            val annenTjenesteOk = pakkseddel.annenTjenesteOk
            if (kontrollTjenesteOk && annenTjenesteOk) return

            val message = pakkseddel.merknaderAsString()

            if (kontrollTjenesteOk) {
                throw KanIkkeBeregnesException(message, pakkseddel.merknadListe)
            } else {
                throw BeregningsmotorValidereException(message, pakkseddel.merknadListe)
            }
        }

        private fun personOpptjeningsgrunnlag(opptjeningGrunnlag: Opptjeningsgrunnlag, foedselDato: LocalDate?) =
            PersonOpptjeningsgrunnlag().apply {
                this.opptjening = opptjeningGrunnlag
                this.fodselsdato = fromLocalDate(foedselDato)
            }

        private fun updatePersonOpptjeningsFieldFromReglerResponse(
            originalList: List<PersonOpptjeningsgrunnlag>,
            regelResponseList: List<PersonOpptjeningsgrunnlag>
        ) {
            val map: MutableMap<String, Opptjeningsgrunnlag> = HashMap()

            for (requestPersonOpptjeningsgrunnlag in originalList) {
                val key = getKey(requestPersonOpptjeningsgrunnlag)
                map[key] = requestPersonOpptjeningsgrunnlag.opptjening!!
            }

            for (regelOpptjeningsGrunnlag in regelResponseList) {
                copyUpdatedData(regelOpptjeningsGrunnlag.opptjening!!, map[getKey(regelOpptjeningsGrunnlag)]!!)
            }
        }

        private fun copyUpdatedData(source: Opptjeningsgrunnlag, target: Opptjeningsgrunnlag) {
            target.pp = source.pp
            target.pi = source.pi
            target.pia = source.pia
            target.ar = source.ar
            target.opptjeningType = source.opptjeningType
            target.bruk = source.bruk
            target.grunnlagKilde = source.grunnlagKilde
        }

        /**
         * Format: "pid:år"
         */
        private fun getKey(grunnlag: PersonOpptjeningsgrunnlag): String {
            val key = StringBuilder()

            if (grunnlag.fnr != null) {
                key.append(grunnlag.fnr)
            }

            key.append(":").append(grunnlag.opptjening!!.ar)
            return key.toString()
        }
    }
}
