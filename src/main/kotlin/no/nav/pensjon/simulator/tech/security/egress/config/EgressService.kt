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
    FSS_GATEWAY(
        description = "FSS-gateway",
        shortName = "FGW",
        purpose = "Proxy for SOAP-kall til tjenestepensjonsordning"
    ),
    AFP_BEHOLDNING_API(
        description = "AFP Beholdning API",
        shortName = "AFP-BEHOLDNING-API",
        purpose = "Hente simulerte AFP-beholdninger"
    ),
    PENSJONSFAGLIG_KJERNE(
        description = "Pensjonsfaglig kjerne",
        shortName = "PEN",
        purpose = "Hente pensjonsdata"
    ),
    PENSJON_PERSONDATA(
        description = "Pensjonsrelaterte persondata",
        shortName = "PPD",
        purpose = "Hente pensjonsrelaterte persondata"
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
        purpose = "Hente opptjeningsgrunnlag",
        authType = AuthType.PERSONAL_NOT_TOKEN_X
    ),
    PERSONDATA(
        description = "Persondata",
        shortName = "PDL",
        purpose = "Hente generelle persondata",
        authType = AuthType.PERSONAL
    ),
    SPORINGSLOGG(
        description = "Sporingslogg",
        shortName = "SPOR",
        purpose = "Logge datautveksling med eksterne parter"
    ),
    TP_REGISTERET(
        description = "Tjenestepensjonsregisteret",
        shortName = "TP",
        purpose = "Hente tjenestepensjonsforhold"
    ),

    // Eksterne tjenester:
    SPK(
        description = "Statens Pensjonskasse",
        shortName = "SPK",
        purpose = "Simulere offentlig tjenestepensjon",
        authType = AuthType.MACHINE_OUTSIDE_NAV
    ),
    STOREBRAND(
        description = "Storebrand",
        shortName = "STOREBRAND",
        purpose = "Simulere offentlig tjenestepensjon",
        authType = AuthType.MACHINE_OUTSIDE_NAV
    ),
    KLP(
        description = "Kommunal Landspensjonskasse",
        shortName = "KLP",
        purpose = "Simulere offentlig tjenestepensjon",
        authType = AuthType.MACHINE_OUTSIDE_NAV
    ),

    // Felles-konfigurasjon for tjenester som leverer egress-token:
    OAUTH2_TOKEN(
        description = "OAuth2 token",
        shortName = "OA2",
        purpose = "OAuth2 access token",
        authType = AuthType.NONE
    )
}