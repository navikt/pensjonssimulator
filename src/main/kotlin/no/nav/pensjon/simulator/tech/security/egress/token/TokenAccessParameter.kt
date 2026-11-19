package no.nav.pensjon.simulator.tech.security.egress.token

import no.nav.pensjon.simulator.tech.security.egress.oauth2.AuthorizationGrantType

class TokenAccessParameter(private val type: AuthorizationGrantType, val value: String) {

    fun getGrantTypeName(): String = type.value

    fun getParameterName(): String = type.parameterName

    companion object {
        fun clientCredentials(scope: String) =
            TokenAccessParameter(type = AuthorizationGrantType.CLIENT_CREDENTIALS, value = scope)

        fun jwtBearer(assertion: String) =
            TokenAccessParameter(type = AuthorizationGrantType.JWT_BEARER, value = assertion)

        fun tokenExchange(subjectToken: String) =
            TokenAccessParameter(type = AuthorizationGrantType.TOKEN_EXCHANGE, value = subjectToken)
    }
}