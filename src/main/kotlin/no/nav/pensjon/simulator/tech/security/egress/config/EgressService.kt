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
    PENSJONSFAGLIG_KJERNE(
        description = "Pensjonsfaglig kjerne",
        shortName = "PEN",
        purpose = "Finne tidligst mulig uttak"
    ),
    SPORINGSLOGG(
        description = "Sporingslogg",
        shortName = "SL",
        purpose = "Logge utleverte data"
    )
}
