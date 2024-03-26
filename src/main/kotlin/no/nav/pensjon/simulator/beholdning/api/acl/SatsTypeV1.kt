package no.nav.pensjon.simulator.beholdning.api.acl

import no.nav.pensjon.simulator.beholdning.SatsType

enum class SatsTypeV1(val externalValue: String, val internalValue: SatsType) {

    NONE("", SatsType.NONE),
    UNKNOWN("?", SatsType.UNKNOWN),
    ORDINAER("ORDINAER", SatsType.ORDINAER),
    HOEY("HOY", SatsType.HOEY);

    companion object {
        private val values = entries.toTypedArray()

        fun fromInternalValue(value: SatsType): SatsTypeV1 = values.single { it.internalValue == value }
    }
}
