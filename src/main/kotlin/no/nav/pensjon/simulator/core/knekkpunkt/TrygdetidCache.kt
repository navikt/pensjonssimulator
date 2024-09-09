package no.nav.pensjon.simulator.core.knekkpunkt

import no.nav.pensjon.simulator.core.exception.BeregningsmotorValidereException
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.GrunnlagRolle
import no.nav.pensjon.simulator.core.domain.regler.to.TrygdetidRequest
import no.nav.pensjon.simulator.core.exception.BeregningstjenesteFeiletException
import no.nav.pensjon.simulator.core.exception.KanIkkeBeregnesException
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getYear

// Corresponds to FastsettTrygdetidCache
class TrygdetidCache(val context: SimulatorContext) {

    private val uberCache: MutableMap<GrunnlagRolle, TrygdetidInternalCache> = mutableMapOf()

    // FastsettTrygdetidCache.createCacheForGrunnlagsRolleCodes
    fun createCacheForGrunnlagsroller(vararg grunnlagsroller: GrunnlagRolle) {
        for (grunnlagsrolle in grunnlagsroller) {
            uberCache[grunnlagsrolle] = TrygdetidInternalCache()
        }
    }

    //@Throws(BeregningstjenesteFeiletException::class)
    fun fastsettTrygdetid(
        parameters: TrygdetidRequest,
        grunnlagsrolle: GrunnlagRolle,
        kravIsUforetrygd: Boolean,
        sakId: Long?
    ): TrygdetidCombo {
        validate(parameters)

        // **** NB: in the original updateSisteGyldigeOpptjeningsaar method in FastsettTrygdetidCache
        // **** the following modification of persongrunnlag was performed:
        // updateSisteGyldigeOpptjeningsaar(parameters)
        // **** Such modification should not be made here, and hence this has been moved to SimulatorTrygdetidFastsetter

        return trygdetidCache(grunnlagsrolle)
            ?.let { fastsettTrygdetidWithCache(it, parameters, kravIsUforetrygd, sakId) }
            ?: refreshFastsettTrygdetid(parameters, kravIsUforetrygd, sakId)
    }

    // FastsettTrygdetidCache.getTrygdetidCacheFromGrunnlagsrolle
    private fun trygdetidCache(grunnlagsrolle: GrunnlagRolle): TrygdetidInternalCache? =
        if (uberCache.containsKey(grunnlagsrolle))
            uberCache[grunnlagsrolle]
        else
            null

    // FastsettTrygdetidCache.fastsettTrygdetidInPreg
    //@Throws(BeregningstjenesteFeiletException::class)
    private fun refreshFastsettTrygdetid(
        parameters: TrygdetidRequest,
        kravIsUforetrygd: Boolean,
        sakId: Long?
    ): TrygdetidCombo =
        try {
            context.refreshFastsettTrygdetid(parameters, kravIsUforetrygd, sakId)
                .let { TrygdetidCombo(it.trygdetid, it.trygdetidKapittel20) }
        } catch (e: KanIkkeBeregnesException) {
            throw BeregningstjenesteFeiletException(e)
        } catch (e: BeregningsmotorValidereException) {
            throw BeregningstjenesteFeiletException(e)
        }

    //@Throws(BeregningstjenesteFeiletException::class)
    private fun fastsettTrygdetidWithCache(
        cache: TrygdetidInternalCache,
        parameters: TrygdetidRequest,
        kravIsUforetrygd: Boolean,
        sakId: Long?
    ): TrygdetidCombo? {
        val year = parameters.virkFom?.let(::getYear)!!

        return when {
            cache.containsTrygdetid(year) -> cache.trygdetid(year)!!
            cache.isLatestTrygdetidMaxTrygdetidBeforeYear(year) -> cache.latestTrygdetidBeforeYear(year)
                ?.also { cache.addTrygdetid(year, it) }

            else -> refreshFastsettTrygdetid(parameters, kravIsUforetrygd, sakId).also { cache.addTrygdetid(year, it) }
        }
    }

    private fun validate(parameters: TrygdetidRequest) {
        if (parameters.virkFom == null) {
            throw UnsupportedOperationException("No virkFom was specified. The value is mandatory when using the cache.")
        }
    }
}
