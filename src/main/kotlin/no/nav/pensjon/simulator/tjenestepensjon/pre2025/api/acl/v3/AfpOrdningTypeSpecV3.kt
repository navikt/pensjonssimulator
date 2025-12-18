package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3

enum class AfpOrdningTypeSpecV3(val decode: String) {
    AFPKOM("AFP - Kommunalsektor"),
    AFPSTAT("AFP - Stat"),
    FINANS("Finansnæringen"),
    KONV_K("Konvertert privat"),
    KONV_O("Konvertert offentlig"),
    LONHO("LO/NHO - ordningen"),
    NAVO("Spekter");

    val code: String
        get() = name
}
