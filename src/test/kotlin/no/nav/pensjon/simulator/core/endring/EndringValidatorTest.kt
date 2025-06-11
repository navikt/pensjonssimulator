package no.nav.pensjon.simulator.core.endring

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.exception.InvalidArgumentException
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec

class EndringValidatorTest : FunSpec({

    test("'validate' should throw exception if simuleringstype ikke gjelder endring") {
        shouldThrow<InvalidArgumentException> {
            EndringValidator.validate(simuleringSpec(type = SimuleringTypeEnum.ALDER))
        }.message shouldBe "Invalid simuleringstype: ALDER"
    }
})
