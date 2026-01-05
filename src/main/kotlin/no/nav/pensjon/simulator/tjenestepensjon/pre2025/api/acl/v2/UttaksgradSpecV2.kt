package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import no.nav.pensjon.simulator.core.krav.UttakGradKode

// no.nav.domain.pensjon.kjerne.kodetabeller.UttaksgradCode
enum class UttaksgradSpecV2(@get:JsonValue val externalValue: String, val internalValue: UttakGradKode) {
    /**
     * 0 %
     */
    P_0("0", UttakGradKode.P_0),

    /**
     * 100 %
     */
    P_100("100", UttakGradKode.P_100),

    /**
     * 20 %
     */
    P_20("20", UttakGradKode.P_20),

    /**
     * 40 %
     */
    P_40("40", UttakGradKode.P_40),

    /**
     * 50 %
     */
    P_50("50", UttakGradKode.P_50),

    /**
     * 60 %
     */
    P_60("60", UttakGradKode.P_60),

    /**
     * 80 %
     */
    P_80("80", UttakGradKode.P_80);

    companion object {
        @JvmStatic
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        fun fromJson(v: Any?) = entries.firstOrNull { it.externalValue.equals(v?.toString(), ignoreCase = true) }
            ?: throw IllegalArgumentException(
                "$v is not valid. Allowed values: " + entries.joinToString(",") { it.externalValue }
            )

        fun toInt(v: UttaksgradSpecV2?) = v?.externalValue?.toInt() ?: 100
    }
}
