package no.nav.pensjon.simulator.tjenestepensjon.pre2025

enum class StillingsprosentCode(val externalValue: String) {
    P_0("0"),
    P_10("10"),
    P_20("20"),
    P_30("30"),
    P_40("40"),
    P_50("50"),
    P_60("60"),
    P_70("70"),
    P_75("75"),
    P_80("80"),
    P_90("90"),
    P_100("100");

    companion object {
        fun fromJson(v: Any?): StillingsprosentCode =
            entries.firstOrNull { it.externalValue.equals(v?.toString(), ignoreCase = true) }
                ?: throw IllegalArgumentException(
                    "$v is not valid. Allowed: " + entries.joinToString(",") { it.externalValue }
                )

        fun toInt(v: StillingsprosentCode?) = v?.externalValue?.toInt()
    }
}