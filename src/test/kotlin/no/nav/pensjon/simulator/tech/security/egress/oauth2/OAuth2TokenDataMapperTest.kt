package no.nav.pensjon.simulator.tech.security.egress.oauth2

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class OAuth2TokenDataMapperTest : FunSpec({

    test("'map' should map values") {
        val dto = OAuth2TokenDto().apply {
            setAccessToken("access-token")
            setIdToken("id-token")
            setRefreshToken("refresh-token")
            setExpiresIn(1)
        }

        val tokenData = OAuth2TokenDataMapper.map(dto, LocalDateTime.MIN)

        with(tokenData) {
            accessToken shouldBe dto.getAccessToken()
            idToken shouldBe dto.getIdToken()
            refreshToken shouldBe dto.getRefreshToken()
            issuedTime shouldBe LocalDateTime.MIN
            expiresInSeconds shouldBe 1L
        }
    }

    test("'map' should allow null values for ID token and refresh token") {
        val dto = OAuth2TokenDto().apply {
            setIdToken(null)
            setRefreshToken(null)
            setAccessToken("access-token")
            setExpiresIn(1)
        }

        val tokenData = OAuth2TokenDataMapper.map(dto, LocalDateTime.MIN)

        with(tokenData) {
            idToken shouldBe ""
            refreshToken shouldBe ""
        }
    }
})
