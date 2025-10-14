package no.nav.pensjon.simulator.tech.security.ingress

import no.nav.pensjon.simulator.tech.security.ingress.jwt.authoritiesFromScope
import no.nav.pensjon.simulator.tech.security.ingress.jwt.consumerOrganisasjonsnummer
import no.nav.pensjon.simulator.tech.security.ingress.jwt.supplierOrganisasjonsnummer
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class MaskinportenAuthenticationConverter : Converter<Jwt, AbstractAuthenticationToken> {

    override fun convert(token: Jwt): AbstractAuthenticationToken =
        token.consumerOrganisasjonsnummer().let {
            JwtAuthenticationToken(
                token,
                listOfNotNull(
                    *token.authoritiesFromScope().toTypedArray(),
                    ConsumerOrganizationGrantedAuthority(organisasjonsnummer = it),
                    token.supplierOrganisasjonsnummer()?.let(::SupplierOrganizationGrantedAuthority)
                ),
                it
            )
        }
}

data class ConsumerOrganizationGrantedAuthority(val organisasjonsnummer: String) : GrantedAuthority {
    override fun getAuthority(): String =
        "consumer-organisation:$organisasjonsnummer"
}

data class SupplierOrganizationGrantedAuthority(val organisasjonsnummer: String) : GrantedAuthority {
    override fun getAuthority(): String =
        "supplier-organisation:$organisasjonsnummer"
}
