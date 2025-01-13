package no.nav.pensjon.simulator.core.knekkpunkt

import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.to.TrygdetidRequest
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getYear
import no.nav.pensjon.simulator.core.util.PensjonTidUtil.OPPTJENING_ETTERSLEP_ANTALL_AAR
import org.springframework.stereotype.Component

@Component
class TrygdetidFastsetter(private val context: SimulatorContext) {

    private var trygdetidCache: TrygdetidCache? = null

    // VilkarsprovOgBeregnAlderHelper.fastsettTrygdetidForPeriode + FastsettTrygdetidCache.updateSisteGyldigeOpptjeningsaar
    fun fastsettTrygdetidForPeriode(
        spec: TrygdetidRequest,
        rolle: GrunnlagsrolleEnum,
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
        rolle: GrunnlagsrolleEnum,
        gjelderUfoeretrygd: Boolean,
        sakId: Long?
    ): TrygdetidCombo {
        /*
        TODO restore cache?
        NB: This cache cannot be used by consecutive simuleringer
        if (trygdetidCache == null) {
            trygdetidCache = TrygdetidCache(context = context)
                .also { it.createCacheForGrunnlagsroller(GrunnlagsrolleEnum.SOKER, GrunnlagsrolleEnum.AVDOD) }
        }

        return trygdetidCache!!.fastsettTrygdetid(spec, rolle, gjelderUfoeretrygd, sakId)
        */
        return TrygdetidCache(context).fastsettTrygdetid(spec, rolle, gjelderUfoeretrygd, sakId)
    }
}
