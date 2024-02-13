package no.nav.pensjon.simulator.tech.security.egress.oauth2

import no.nav.pensjon.simulator.tech.security.egress.token.TokenData
import java.time.LocalDateTime

object OAuth2TokenDataMapper {

    fun map(dto: OAuth2TokenDto, time: LocalDateTime) =
        TokenData(
            dto.getAccessToken()!!,
            dto.getIdToken() ?: "",
            dto.getRefreshToken() ?: "",
            time,
            dto.getExpiresIn()!!.toLong()
        )
}
