package no.nav.pensjon.simulator.alderspensjon.regel

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.exception.PersonForGammelException

class RegelverkUtilTest : ShouldSpec({

    should("throw 'person for gammel' exception hvis født før 1943") {
        shouldThrow<PersonForGammelException> {
            RegelverkUtil.regelverkType(foedselsaar = 1942)
        }.message shouldBe "Regelverk for personer født før 1943 er ikke støttet"
    }

    should("gi type 'nytt regelverk, gammel opptjening' hvis født 1943-1953") {
        RegelverkUtil.regelverkType(foedselsaar = 1953) shouldBe RegelverkTypeEnum.N_REG_G_OPPTJ
    }

    should("gi type 'nytt regelverk, gammel og ny opptjening' hvis født 1954-1962") {
        RegelverkUtil.regelverkType(foedselsaar = 1962) shouldBe RegelverkTypeEnum.N_REG_G_N_OPPTJ
    }

    should("gi type 'nytt regelverk, ny opptjening' hvis født 1963 eller senere") {
        RegelverkUtil.regelverkType(foedselsaar = 1963) shouldBe RegelverkTypeEnum.N_REG_N_OPPTJ
    }
})
