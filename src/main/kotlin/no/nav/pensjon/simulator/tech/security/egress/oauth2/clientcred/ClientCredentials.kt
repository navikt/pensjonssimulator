package no.nav.pensjon.simulator.tech.security.egress.oauth2.clientcred

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ClientCredentials(
    @Value("\${azure-app.client-id}") val clientId: String,
    @Value("\${azure-app.client-secret}") val clientSecret: String
)
