package no.nav.pensjon.simulator.tech.security.egress.token

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.tech.representasjon.RepresentertRolle
import no.nav.pensjon.simulator.tech.security.egress.AuthType
import no.nav.pensjon.simulator.tech.security.egress.config.EgressTokenTypeDecisionBasis
import no.nav.pensjon.simulator.tech.security.egress.oauth2.clientcred.ClientCredentialsEgressTokenService
import no.nav.pensjon.simulator.tech.security.egress.oauth2.maskinporten.MaskinportenTokenService
import no.nav.pensjon.simulator.tech.security.egress.oauth2.onbehalfof.OnBehalfOfEgressTokenService
import no.nav.pensjon.simulator.tech.security.egress.oauth2.tokenexchange.TokenExchangeService

class EgressAccessTokenFacadeTest : ShouldSpec({

    context("ingress: borger-token (TokenX) mottatt") {
        context("egress: app støtter borger-token") {
            should("gi vekslet borger-token") {
                testMedBorgerToken(AuthType.PERSONAL) shouldBe "tx-token-out"
            }
        }

        context("egress: app støtter ansatt-token men ikke borger-token") {
            should("gi 'client credentials'-token") {
                testMedBorgerToken(AuthType.PERSONAL_NOT_TOKEN_X) shouldBe "cc-token-out"
            }
        }

        context("egress: app støtter bare app-til-app-token (client credentials)") {
            should("gi app-til-app-token") {
                testMedBorgerToken(AuthType.MACHINE_INSIDE_NAV) shouldBe "cc-token-out"
            }
        }

        context("egress: app støtter bare maskin-token (fra Maskinporten)") {
            should("gi maskin-token") {
                testMedBorgerToken(AuthType.MACHINE_OUTSIDE_NAV) shouldBe "mp-token"
            }
        }

        context("egress: app krever ingen 'Bearer'-autentisering") {
            should("gi tomt resultat") {
                testMedBorgerToken(AuthType.NONE) shouldBe ""
            }
        }
    }

    context("ingress: ansatt-token (on behalf of) mottatt") {
        context("egress: app støtter ansatt-token (og borger-token)") {
            should("gi vekslet ansatt-token") {
                testMedAnsattToken(AuthType.PERSONAL) shouldBe "obo-token-out"
            }
        }

        context("egress: app støtter bare app-til-app-token (client credentials)") {
            should("gi app-til-app-token") {
                testMedAnsattToken(AuthType.MACHINE_INSIDE_NAV) shouldBe "cc-token-out"
            }
        }

        context("egress: app støtter bare maskin-token (fra Maskinporten)") {
            should("gi maskin-token") {
                testMedAnsattToken(AuthType.MACHINE_OUTSIDE_NAV) shouldBe "mp-token"
            }
        }

        context("egress: app krever ingen 'Bearer'-autentisering") {
            should("gi tomt resultat") {
                testMedAnsattToken(AuthType.NONE) shouldBe ""
            }
        }
    }

    context("ingress: app-til-app-token (client credentials) mottatt") {
        context("egress: app støtter ansatt-token (og borger-token)") {
            should("gi app-til-app-token for egress-kall") {
                testMedAppTilAppToken(AuthType.PERSONAL) shouldBe "cc-token-out"
            }
        }

        context("egress: app støtter ansatt-token men ikke borger-token") {
            should("gi app-til-app-token for egress-kall") {
                testMedAppTilAppToken(AuthType.PERSONAL_NOT_TOKEN_X) shouldBe "cc-token-out"
            }
        }

        context("egress: app støtter bare app-til-app-token (client credentials)") {
            should("gi app-til-app-token for egress-kall") {
                testMedAppTilAppToken(AuthType.MACHINE_INSIDE_NAV) shouldBe "cc-token-out"
            }
        }

        context("egress: app støtter bare maskin-token (fra Maskinporten)") {
            should("gi maskin-token") {
                testMedAppTilAppToken(AuthType.MACHINE_OUTSIDE_NAV) shouldBe "mp-token"
            }
        }

        context("egress: app krever ingen 'Bearer'-autentisering") {
            should("gi tomt resultat") {
                testMedAppTilAppToken(AuthType.NONE) shouldBe ""
            }
        }
    }
})

private fun testMedBorgerToken(authType: AuthType): String =
    facade(ingressToken = "tx-token-in").getAccessToken(
        authType,
        audience = "a1",
        tokenTypeDecisionBasis = basisForBorgerToken()
    ).value

private fun testMedAnsattToken(authType: AuthType): String =
    facade(ingressToken = "obo-token-in").getAccessToken(
        authType,
        audience = "a1",
        tokenTypeDecisionBasis = basisForAnsattToken()
    ).value

private fun testMedAppTilAppToken(authType: AuthType): String =
    facade(ingressToken = "cc-token-in").getAccessToken(
        authType,
        audience = "a1",
        tokenTypeDecisionBasis = basisForAppTilAppToken()
    ).value

private fun basisForBorgerToken() =
    EgressTokenTypeDecisionBasis(
        rolle = RepresentertRolle.SELV,
        ingressToken = "tx-token-in"
    )

private fun basisForAnsattToken() =
    EgressTokenTypeDecisionBasis(
        rolle = RepresentertRolle.UNDER_VEILEDNING,
        ingressToken = "obo-token-in"
    )

private fun basisForAppTilAppToken() =
    EgressTokenTypeDecisionBasis(
        rolle = RepresentertRolle.NONE,
        ingressToken = "cc-token-in"
    )

private fun facade(ingressToken: String) =
    EgressAccessTokenFacade(
        clientCredentialsTokenService = arrangeClientCredentials(ingressToken),
        tokenExchangeService = arrangeTokenX(),
        onBehalfOfTokenService = arrangeOnBehalfOf(ingressToken),
        maskinportenTokenService = arrangeMaskinporten(ingressToken)
    )

private fun arrangeClientCredentials(ingressToken: String): ClientCredentialsEgressTokenService =
    mockk {
        every { getEgressToken(ingressToken, audience = "a1") } returns RawJwt(value = "cc-token-out")
    }

private fun arrangeMaskinporten(ingressToken: String): MaskinportenTokenService =
    mockk {
        every { getEgressToken(ingressToken, audience = "a1") } returns RawJwt(value = "mp-token")
    }

private fun arrangeTokenX(): TokenExchangeService =
    mockk {
        every { getEgressToken(ingressToken = "tx-token-in", audience = "a1") } returns RawJwt(value = "tx-token-out")
    }

private fun arrangeOnBehalfOf(ingressToken: String): OnBehalfOfEgressTokenService =
    mockk {
        every { getEgressToken(ingressToken, audience = "a1") } returns RawJwt(value = "obo-token-out")
    }