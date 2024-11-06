package no.nav.pensjon.simulator.tech.security.egress.config

import no.nav.pensjon.simulator.tech.security.egress.AuthType

/**
 * Specifies the services accessed by pensjonssimulator, and their characteristics.
 */
enum class EgressService(
    val description: String,
    val shortName: String,
    val purpose: String,
    val authType: AuthType = AuthType.MACHINE_INSIDE_NAV
) {
    OAUTH2_TOKEN(description = "OAuth2 token", shortName = "OA2", purpose = "OAuth2 access token"),
    FSS_GATEWAY(
        description = "FSS-gateway",
        shortName = "FGW",
        purpose = "Proxy for pensjon-regler og sporingslogg"
    ),
    PENSJONSFAGLIG_KJERNE(
        description = "Pensjonsfaglig kjerne",
        shortName = "PEN",
        purpose = "Finne tidligst mulig uttak"
    ),
    TP_REGISTERET(
        description = "Tjenestepensjonsregisteret",
        shortName = "TP",
        purpose = "Hente tjenestepensjonsforhold"
    ),
    TJENESTEPENSJON_SIMULERING(
        description = "Tjenestepensjon-simulering",
        shortName = "TP-S",
        purpose = "Simulere offentlig tjenestepensjon"
    )
}
