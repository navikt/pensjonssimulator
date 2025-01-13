package no.nav.pensjon.simulator.core.knekkpunkt

import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.to.TrygdetidRequest
import no.nav.pensjon.simulator.core.exception.BeregningsmotorValidereException
import no.nav.pensjon.simulator.core.exception.BeregningstjenesteFeiletException
import no.nav.pensjon.simulator.core.exception.KanIkkeBeregnesException
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getYear

// Corresponds to FastsettTrygdetidCache
class TrygdetidCache(val context: SimulatorContext) {

    private val uberCache: MutableMap<GrunnlagsrolleEnum, TrygdetidInternalCache> = mutableMapOf()

    // FastsettTrygdetidCache.createCacheForGrunnlagsRolleCodes
    fun createCacheForGrunnlagsroller(vararg grunnlagsroller: GrunnlagsrolleEnum) {
        for (grunnlagsrolle in grunnlagsroller) {
            uberCache[grunnlagsrolle] = TrygdetidInternalCache()
        }
    }

    //@Throws(BeregningstjenesteFeiletException::class)
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
    private fun trygdetidCache(grunnlagsrolle: GrunnlagsrolleEnum): TrygdetidInternalCache? =
        if (uberCache.containsKey(grunnlagsrolle))
            uberCache[grunnlagsrolle]
        else
            null

    // FastsettTrygdetidCache.fastsettTrygdetidInPreg
    //@Throws(BeregningstjenesteFeiletException::class)
    private fun refreshFastsettTrygdetid(
        parameters: TrygdetidRequest,
        gjelderUfoeretrygd: Boolean,
        sakId: Long?
    ): TrygdetidCombo =
        try {
            context.refreshFastsettTrygdetid(parameters, gjelderUfoeretrygd, sakId)
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
