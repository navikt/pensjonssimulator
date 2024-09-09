package no.nav.pensjon.simulator.core.knekkpunkt

import no.nav.pensjon.simulator.core.domain.regler.Trygdetid

data class TrygdetidCombo(
    val kapittel19: Trygdetid?,
    val kapittel20: Trygdetid?
) {
    // FastsettTrygdetidInternalCache.latestTrygdetidHasMaxTrygdetid + onlyHasKap19, onlyHasKap20, hasKap19And20
    fun hasMaxTrygdetid(): Boolean =
        when {
            kapittel19 != null && kapittel20 == null -> hasMaxTrygdetid(kapittel19)
            kapittel19 != null && kapittel20 != null -> hasMaxTrygdetid(kapittel19) && hasMaxTrygdetid(kapittel20)
            kapittel19 == null && kapittel20 != null -> hasMaxTrygdetid(kapittel20)
            else -> false
        }

    private companion object {
        private const val MAX_TRYGDETID_ANTALL_AAR: Int = 40

        private fun hasMaxTrygdetid(trygdetid: Trygdetid): Boolean =
            trygdetid.tt == MAX_TRYGDETID_ANTALL_AAR
    }
}
