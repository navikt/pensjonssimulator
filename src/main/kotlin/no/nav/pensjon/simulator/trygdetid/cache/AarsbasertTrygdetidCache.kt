package no.nav.pensjon.simulator.trygdetid.cache

import no.nav.pensjon.simulator.trygdetid.TrygdetidCombo
import java.util.SortedMap
import java.util.TreeMap

// PEN: FastsettTrygdetidInternalCache
class AarsbasertTrygdetidCache {

    private val cache: SortedMap<Int, TrygdetidCombo> = TreeMap()

    fun containsTrygdetid(year: Int): Boolean =
        cache.containsKey(year)

    // FastsettTrygdetidInternalCache.getTrygdetid
    fun trygdetid(year: Int): TrygdetidCombo? =
        cache[year]

    fun addTrygdetid(year: Int, trygdetid: TrygdetidCombo) {
        cache[year] = trygdetid
    }

    // FastsettTrygdetidInternalCache.isLatestTrygdetid40yearsBeforeYear
    fun isLatestTrygdetidMaxTrygdetidBeforeYear(year: Int): Boolean =
        latestTrygdetidBeforeYear(year)?.hasMaxTrygdetid() == true

    // FastsettTrygdetidInternalCache.getLatestTrygdetidBeforeYear
    fun latestTrygdetidBeforeYear(year: Int): TrygdetidCombo? {
        val subcache = cache.subMap(0, year)
        val lastKey: Int? = if (subcache.isEmpty()) null else subcache.lastKey()
        return lastKey?.let { subcache[it] }
    }
}