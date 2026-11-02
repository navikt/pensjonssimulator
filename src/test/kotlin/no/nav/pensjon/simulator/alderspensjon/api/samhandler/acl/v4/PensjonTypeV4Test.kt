package no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v4

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alderspensjon.PensjonType
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException

class PensjonTypeV4Test : ShouldSpec({

    context("funksjonen 'fromInternalValue'") {
        context("mappet internverdi") {
            should("gi eksternverdien") {
                PensjonTypeV4.fromInternalValue(PensjonType.INNTEKTSPENSJON).externalValue shouldBe "inntektsPensjon"
            }
        }

        context("umappet internverdi") {
            should("gi feilmelding") {
                shouldThrow<InvalidEnumValueException> {
                    PensjonTypeV4.fromInternalValue(PensjonType.NONE)
                }.message shouldBe "Mangler V4-mapping for intern pensjonstype NONE"
            }
        }
    }
})