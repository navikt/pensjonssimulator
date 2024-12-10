package no.nav.pensjon.simulator.beholdning.api.acl

import no.nav.pensjon.simulator.beholdning.SatsType

enum class SatsTypeV1(val externalValue: String, val internalValue: SatsType) {

    NONE("", SatsType.NONE),
    UNKNOWN("?", SatsType.UNKNOWN),
    ORDINAER("ORDINAER", SatsType.ORDINAER),
    HOEY("HOY", SatsType.HOEY);

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromInternalValue(value: SatsType): SatsTypeV1 = entries.single { it.internalValue == value }
    }
}
