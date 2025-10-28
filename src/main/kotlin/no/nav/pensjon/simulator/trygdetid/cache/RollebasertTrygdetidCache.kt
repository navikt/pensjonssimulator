package no.nav.pensjon.simulator.trygdetid.cache

import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.to.TrygdetidRequest
import no.nav.pensjon.simulator.core.legacy.util.DateUtil
import no.nav.pensjon.simulator.trygdetid.TrygdetidCombo

// PEN: FastsettTrygdetidCache
class RollebasertTrygdetidCache(val context: SimulatorContext) {

    private val uberCache: MutableMap<GrunnlagsrolleEnum, AarsbasertTrygdetidCache> = mutableMapOf()

    // FastsettTrygdetidCache.createCacheForGrunnlagsRolleCodes
    fun createCacheForGrunnlagsroller(vararg grunnlagsroller: GrunnlagsrolleEnum) {
        for (grunnlagsrolle in grunnlagsroller) {
            uberCache[grunnlagsrolle] = AarsbasertTrygdetidCache()
        }
    }

    fun fastsettTrygdetid(
        parameters: TrygdetidRequest,
        grunnlagsrolle: GrunnlagsrolleEnum,
        gjelderUfoeretrygd: Boolean,
        sakId: Long?
    ): TrygdetidCombo {
        validate(parameters)

        // **** NB: in the original updateSisteGyldigeOpptjeningsaar method in FastsettTrygdetidCache
        // **** the following modification of persongrunnlag was performed:
        // updateSisteGyldigeOpptjeningsaar(parameters)
        // **** Such modification should not be made here, and hence this has been moved to SimulatorTrygdetidFastsetter

        /*
        //TODO Drop using cache, due to PEK-559 (trygdetid depends also on inntektsår and possibly other parameters)
        return trygdetidCache(grunnlagsrolle)
            ?.let { fastsettTrygdetidWithCache(it, parameters, gjelderUfoeretrygd, sakId) }
            ?: refreshFastsettTrygdetid(parameters, gjelderUfoeretrygd, sakId)
        */
        return refreshFastsettTrygdetid(parameters, gjelderUfoeretrygd, sakId)
    }

    // FastsettTrygdetidCache.getTrygdetidCacheFromGrunnlagsrolle
    private fun trygdetidCache(grunnlagsrolle: GrunnlagsrolleEnum): AarsbasertTrygdetidCache? =
        if (uberCache.containsKey(grunnlagsrolle))
            uberCache[grunnlagsrolle]
        else
            null

    // FastsettTrygdetidCache.fastsettTrygdetidInPreg
    private fun refreshFastsettTrygdetid(
        parameters: TrygdetidRequest,
        gjelderUfoeretrygd: Boolean,
        sakId: Long?
    ): TrygdetidCombo =
        context.refreshFastsettTrygdetid(parameters, gjelderUfoeretrygd, sakId)
            .let { TrygdetidCombo(kapittel19 = it.trygdetid, kapittel20 = it.trygdetidKapittel20) }

    private fun fastsettTrygdetidWithCache(
        cache: AarsbasertTrygdetidCache,
        parameters: TrygdetidRequest,
        gjelderUfoeretrygd: Boolean,
        sakId: Long?
    ): TrygdetidCombo? {
        val year: Int = parameters.virkFom!!.let(DateUtil::getYear)

        return when {
            cache.containsTrygdetid(year) -> cache.trygdetid(year)!!

            cache.isLatestTrygdetidMaxTrygdetidBeforeYear(year) -> cache.latestTrygdetidBeforeYear(year)?.also {
                cache.addTrygdetid(year, trygdetid = it)
            }

            else -> refreshFastsettTrygdetid(parameters, gjelderUfoeretrygd, sakId).also {
                cache.addTrygdetid(year, trygdetid = it)
            }
        }
    }

    private fun validate(parameters: TrygdetidRequest) {
        if (parameters.virkFom == null) {
            throw UnsupportedOperationException("No virkFom was specified. The value is mandatory when using the cache.")
        }
    }
}
