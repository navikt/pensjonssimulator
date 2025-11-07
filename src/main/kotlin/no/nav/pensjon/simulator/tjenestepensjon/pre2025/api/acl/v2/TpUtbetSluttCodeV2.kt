package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class TpUtbetSluttCodeV2(@get:JsonValue val externalValue: String) {
    P_62("62"),
    P_63("63"),
    P_64("64"),
    P_65("65"),
    P_66("66"),
    P_67("67"),
    P_68("68"),
    P_69("69"),
    P_70("70"),
    P_71("71"),
    P_72("72"),
    P_73("73"),
    P_74("74"),
    P_75("75"),
    LIVSVARIG("LIVSVARIG");

    companion object {
        @JvmStatic
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        fun fromJson(v: Any?): TpUtbetSluttCodeV2 {
            val s = v?.toString()?.trim()
            return entries.firstOrNull { it.externalValue.equals(s, ignoreCase = true) }
                ?: throw IllegalArgumentException(
                    "$v is not valid. Allowed: " + entries.joinToString(",") { it.externalValue }
                )
        }
    }
}