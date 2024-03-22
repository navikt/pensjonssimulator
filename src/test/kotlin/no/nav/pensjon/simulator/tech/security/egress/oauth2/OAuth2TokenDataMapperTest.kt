package no.nav.pensjon.simulator.tech.security.egress.oauth2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class OAuth2TokenDataMapperTest {

    private lateinit var dto: OAuth2TokenDto

    @BeforeEach
    fun initialize() {
        dto = OAuth2TokenDto()
    }

    @Test
    fun `map maps values`() {
        dto.setAccessToken("access-token")
        dto.setIdToken("id-token")
        dto.setRefreshToken("refresh-token")
        dto.setExpiresIn(1)

        val tokenData = OAuth2TokenDataMapper.map(dto, LocalDateTime.MIN)

        assertEquals(dto.getAccessToken(), tokenData.accessToken)
        assertEquals(dto.getIdToken(), tokenData.idToken)
        assertEquals(dto.getRefreshToken(), tokenData.refreshToken)
        assertEquals(LocalDateTime.MIN, tokenData.issuedTime)
        assertEquals(1L, tokenData.expiresInSeconds)
    }

    @Test
    fun `map allows null values for ID token and refresh token`() {
        dto.setIdToken(null)
        dto.setRefreshToken(null)
        dto.setAccessToken("access-token")
        dto.setExpiresIn(1)

        val tokenData = OAuth2TokenDataMapper.map(dto, LocalDateTime.MIN)

        assertEquals("", tokenData.idToken)
        assertEquals("", tokenData.refreshToken)
    }
}
