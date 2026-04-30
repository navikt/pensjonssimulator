package no.nav.pensjon.simulator.krav.client.pen

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.trace.TraceAid
import no.nav.pensjon.simulator.tech.web.WebClientBase
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.arrangeOkJsonResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.cache.caffeine.CaffeineCacheManager
import java.time.LocalDate

class PenKravClientTest : ShouldSpec({
    var server: MockWebServer? = null
    var baseUrl: String? = null

    fun client(context: BeanFactory) =
        PenKravClient(
            baseUrl!!,
            retryAttempts = "0",
            webClientBase = context.getBean<WebClientBase>(),
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

    should("gi et deserialisert og domenemappet kravhode") {
        val kravhode: String = this::class.java.getResource("/pen-kravhode.json")?.readText(Charsets.UTF_8)!!
        server!!.arrangeOkJsonResponse(body = kravhode)

        Arrange.webClientContextRunner().run {
            val result: Kravhode = client(context = it).fetchKravhode(kravhodeId = 123L)

            with(result) {
                persongrunnlagListe shouldHaveSize 1
                with(persongrunnlagListe[0]) {
                    personDetaljListe shouldHaveSize 1
                    fodselsdatoLd shouldBe LocalDate.of(1958, 1, 13)
                    with(personDetaljListe[0]) {
                        bruk shouldBe true
                        rolleFomDatoLd shouldBe LocalDate.of(1999, 2, 1)
                    }
                }
                kravlinjeListe shouldHaveSize 1
                kravlinjeListe[0].kravlinjeTypeEnum shouldBe KravlinjeTypeEnum.AP
                sakPenPersonFnr shouldBe Pid("13415812609")
            }
        }
    }
})