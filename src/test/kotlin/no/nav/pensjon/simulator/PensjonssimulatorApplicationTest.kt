package no.nav.pensjon.simulator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class PensjonssimulatorApplicationTest : FunSpec() {

    @Autowired
    lateinit var context: ApplicationContext

    init {
        test("context loads") {
            context.id shouldBe "application"
        }
    }
}
