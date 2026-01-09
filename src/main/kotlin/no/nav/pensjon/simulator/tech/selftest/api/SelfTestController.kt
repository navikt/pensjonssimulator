package no.nav.pensjon.simulator.tech.selftest.api

import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpServletRequest
import no.nav.pensjon.simulator.tech.security.egress.SecurityContextEnricher
import no.nav.pensjon.simulator.tech.selftest.SelfTest
import no.nav.pensjon.simulator.tech.selftest.api.acl.SelfTestResultMapperV1.dto
import no.nav.pensjon.simulator.tech.selftest.api.acl.SelfTestResultV1
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets

@RestController
class SelfTestController(
    private val selfTest: SelfTest,
    private val securityContextEnricher: SecurityContextEnricher
) {
    @GetMapping("internal/selftest")
    @Operation(
        summary = "Selvtest",
        description = "Utfører selvtest for applikasjonen, dvs. pinger applikasjonene den ar avhengig av"
    )
    fun selfTest(request: HttpServletRequest): ResponseEntity<Any> {
        securityContextEnricher.enrichAuthentication(request)
        val accept = request.getHeader(HttpHeaders.ACCEPT)

        return when (accept) {
            MediaType.APPLICATION_JSON_VALUE -> jsonResponseEntity(dto(selfTest.perform()))
            else -> htmlResponseEntity(selfTest.performAndReportAsHtml())
        }
    }

    companion object {
        private fun htmlResponseEntity(htmlResult: String): ResponseEntity<Any> =
            ResponseEntity(
                htmlResult,
                contentTypeHeaders(MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8)),
                HttpStatus.OK
            )

        private fun jsonResponseEntity(result: SelfTestResultV1): ResponseEntity<Any> =
            ResponseEntity(result, contentTypeHeaders(MediaType.APPLICATION_JSON), HttpStatus.OK)

        private fun contentTypeHeaders(mediaType: MediaType) =
            HttpHeaders().apply {
                add(HttpHeaders.CONTENT_TYPE, mediaType.toString())
            }
    }
}