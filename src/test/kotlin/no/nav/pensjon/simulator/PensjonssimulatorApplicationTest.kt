package no.nav.pensjon.simulator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.testconfig.TestConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    properties = [
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
        "spring.flyway.enabled=false"
    ]
)
@ActiveProfiles("test")
@Import(TestConfig::class)
open class PensjonssimulatorApplicationTest : FunSpec() {

    @Autowired
    lateinit var context: ApplicationContext

    init {
        test("context loads") {
            context.id shouldBe "application"
        }
    }
}
