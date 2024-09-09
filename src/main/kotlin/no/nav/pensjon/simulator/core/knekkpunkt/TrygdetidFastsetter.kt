package no.nav.pensjon.simulator.core.knekkpunkt

import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.GrunnlagRolle
import no.nav.pensjon.simulator.core.domain.regler.to.TrygdetidRequest
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getYear
import no.nav.pensjon.simulator.core.util.PensjonTidUtil.OPPTJENING_ETTERSLEP_ANTALL_AAR

class TrygdetidFastsetter(private val context: SimulatorContext) {

    private var trygdetidCache: TrygdetidCache? = null

    // VilkarsprovOgBeregnAlderHelper.fastsettTrygdetidForPeriode + FastsettTrygdetidCache.updateSisteGyldigeOpptjeningsaar
    fun fastsettTrygdetidForPeriode(
        spec: TrygdetidRequest,
        rolle: GrunnlagRolle,
        kravIsUforetrygd: Boolean,
        sakId: Long?
    ): TrygdetidCombo {
        spec.persongrunnlag?.let {
            it.sisteGyldigeOpptjeningsAr = getYear(spec.virkFom!!) - OPPTJENING_ETTERSLEP_ANTALL_AAR
        } // updateSisteGyldigeOpptjeningsaar

        return fastsettTrygdetid(spec, rolle, kravIsUforetrygd, sakId)
    }

    // SimuleringEtter2011Context.fastsettTrygdetid
    //@Throws(PEN222BeregningstjenesteFeiletException::class)
    private fun fastsettTrygdetid(
        spec: TrygdetidRequest,
        rolle: GrunnlagRolle,
        kravIsUfoeretrygd: Boolean,
        sakId: Long?
    ): TrygdetidCombo {
        if (trygdetidCache == null) {
            trygdetidCache = TrygdetidCache(context = context)
                .also { it.createCacheForGrunnlagsroller(GrunnlagRolle.SOKER, GrunnlagRolle.AVDOD) }
        }

        return trygdetidCache!!.fastsettTrygdetid(spec, rolle, kravIsUfoeretrygd, sakId)
    }
}
