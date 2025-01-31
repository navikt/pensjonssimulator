package no.nav.pensjon.simulator.beholdning.api.acl

import no.nav.pensjon.simulator.core.domain.regler.enum.GarantiPensjonsnivaSatsEnum

enum class SatsTypeV1(val externalValue: String, val internalValue: GarantiPensjonsnivaSatsEnum) {

    NONE(externalValue = "", internalValue = GarantiPensjonsnivaSatsEnum.ORDINAER),
    UNKNOWN(externalValue = "?", internalValue = GarantiPensjonsnivaSatsEnum.ORDINAER),
    ORDINAER(externalValue = "ORDINAER", internalValue = GarantiPensjonsnivaSatsEnum.ORDINAER),
    HOEY(externalValue = "HOY", internalValue = GarantiPensjonsnivaSatsEnum.HOY);

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromInternalValue(value: GarantiPensjonsnivaSatsEnum): SatsTypeV1 =
            when (value) {
                GarantiPensjonsnivaSatsEnum.ORDINAER -> ORDINAER // ambiguous UNKNOWN/ORDINAER
                else -> entries.firstOrNull { it.internalValue == value } ?: ORDINAER
            }
    }
}
