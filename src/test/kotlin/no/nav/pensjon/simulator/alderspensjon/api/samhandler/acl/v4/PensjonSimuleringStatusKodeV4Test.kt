package no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v4

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alderspensjon.PensjonSimuleringStatusKode

class PensjonSimuleringStatusKodeV4Test : ShouldSpec({

    context("funksjonen 'fromInternalValue'") {
        context("mappet internverdi") {
            should("gi tilsvarende eksternverdi") {
                PensjonSimuleringStatusKodeV4.fromInternalValue(
                    PensjonSimuleringStatusKode.BRUKER_FOEDT_FOER_1943
                ).externalValue shouldBe "BRUKER_FODT_FOR_1943"
            }
        }

        context("umappet internverdi") {
            should("gi eksternverdien 'annet'") {
                PensjonSimuleringStatusKodeV4.fromInternalValue(
                    PensjonSimuleringStatusKode.NONE
                ).externalValue shouldBe "ANNET"
            }
        }
    }
})