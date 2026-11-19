package no.nav.pensjon.simulator.tech.security.egress

enum class AuthType {
    NONE,
    PERSONAL, // for tjenester som støtter både Entra OBO og TokenX
    PERSONAL_NOT_TOKEN_X, // for tjenester som støtter Entra OBO men ikke TokenX
    MACHINE_INSIDE_NAV, // for tjenester som bare støtter client credentials
    MACHINE_OUTSIDE_NAV
}