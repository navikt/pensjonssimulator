package no.nav.pensjon.simulator.core.knekkpunkt

import java.util.*

// Corresponds to FastsettTrygdetidInternalCache
class TrygdetidInternalCache {

    private val cache: SortedMap<Int, TrygdetidCombo> = TreeMap()

    fun containsTrygdetid(year: Int?): Boolean = year?.let(cache::containsKey) ?: false

    // FastsettTrygdetidInternalCache.getTrygdetid
    fun trygdetid(year: Int?): TrygdetidCombo? = year?.let { cache[it] }

    fun addTrygdetid(year: Int, trygdetid: TrygdetidCombo) {
        cache[year] = trygdetid
    }

    // FastsettTrygdetidInternalCache.isLatestTrygdetid40yearsBeforeYear
    fun isLatestTrygdetidMaxTrygdetidBeforeYear(year: Int?): Boolean =
        latestTrygdetidBeforeYear(year)?.hasMaxTrygdetid() ?: false

    // FastsettTrygdetidInternalCache.getLatestTrygdetidBeforeYear
    fun latestTrygdetidBeforeYear(year: Int?): TrygdetidCombo? {
        if (year == null) return null
        val subcache = cache.subMap(0, year)
        val lastKey = if (subcache.isEmpty()) null else subcache.lastKey()
        return lastKey?.let { subcache[it] }
    }
}
