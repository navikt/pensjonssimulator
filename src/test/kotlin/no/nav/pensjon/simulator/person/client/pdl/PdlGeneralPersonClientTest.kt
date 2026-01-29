package no.nav.pensjon.simulator.person.client.pdl

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.person.Sivilstandstype
import no.nav.pensjon.simulator.person.Person
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.WebClientBase
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.arrangeOkJsonResponse
import okhttp3.mockwebserver.MockWebServer
import org.intellij.lang.annotations.Language
import org.springframework.beans.factory.BeanFactory
import org.springframework.cache.caffeine.CaffeineCacheManager
import java.time.LocalDate

class PdlGeneralPersonClientTest : ShouldSpec({
    var server: MockWebServer? = null
    var baseUrl: String? = null

    fun client(context: BeanFactory) =
        PdlGeneralPersonClient(
            baseUrl!!,
            retryAttempts = "0",
            webClientBase = context.getBean(WebClientBase::class.java),
            cacheManager = CaffeineCacheManager(),
            traceAid = mockk<TraceAid>(relaxed = true),
        )

    beforeSpec {
        Arrange.security()
        server = MockWebServer().apply { start() }
        baseUrl = "http://localhost:${server.port}"
    }

    afterSpec {
        server?.shutdown()
    }

    should("fetch and map fødselsdato + sivilstand") {
        server?.arrangeOkJsonResponse(body = PERSONALIA_JSON)

        Arrange.webClientContextRunner().run {
            client(context = it).fetchPerson(Pid("22426305678")) shouldBe
                    Person(
                        foedselsdato = LocalDate.of(1963, 12, 31),
                        sivilstand = Sivilstandstype.UGIFT,
                        statsborgerskap = LandkodeEnum.LAO
                    )

        }
    }
})

@Language("JSON")
private const val PERSONALIA_JSON = """{
              "data": {
                "hentPerson": {
                  "foedselsdato": [
                    {
                      "foedselsdato": "1963-12-31"
                    }
                  ],
                  "sivilstand": [
                    {
                      "type": "UGIFT"
                    }
                  ],
                  "statsborgerskap": [
                    {
                      "land": "LAO"
                    }
                  ]
                }
              }
            }"""
