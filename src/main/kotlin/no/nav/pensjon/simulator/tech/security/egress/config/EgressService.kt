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
        purpose = "Proxy for sporingslogg"
    ),
    PENSJONSFAGLIG_KJERNE(
        description = "Pensjonsfaglig kjerne",
        shortName = "PEN",
        purpose = "Hente pensjonsdata"
    ),
    PENSJON_REGLER(
        description = "Pensjonsfaglig regelmotor",
        shortName = "PR",
        purpose = "Vilårsprøving og beregning",
        authType = AuthType.NONE
    ),
    PENSJONSOPPTJENING(
        description = "Pensjonsopptjening",
        shortName = "POPP",
        purpose = "Hente opptjeningsgrunnlag"
    ),
    PERSONDATA(
        description = "Persondata",
        shortName = "PDL",
        purpose = "Hente generelle persondata"
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
    ),
    SPK(
        description = "Statens Pensjonskasse",
        shortName = "SPK",
        purpose = "Simulere offentlig tjenestepensjon",
        authType = AuthType.MACHINE_OUTSIDE_NAV
    ),
}
