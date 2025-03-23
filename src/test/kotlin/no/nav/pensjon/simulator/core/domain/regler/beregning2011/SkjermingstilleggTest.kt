package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011.copy
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SkjermingstilleggTest : FunSpec({

    test("'copy' should copy base class values") {
        val source = Skjermingstillegg().apply {
            brutto = 1
            netto = 2
            fradrag = 3
            bruttoPerAr = 1.2
        }

        with(source.copy()) {
            brutto shouldBe 1
            netto shouldBe 2
            fradrag shouldBe 3
            bruttoPerAr shouldBe 1.2
        }
    }
})
