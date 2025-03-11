package no.nav.pensjon.simulator.core.domain.regler.beregning

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.Uforetrygdberegning
import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum

class BeregningRelasjonTest : FunSpec({

    test("copy constructor should copy Uforetrygdberegning") {
        val source = BeregningRelasjon().apply {
            beregning2011 = Uforetrygdberegning().apply {
                bruttoPerAr = 1
                formelKodeEnum = FormelKodeEnum.AFP3
                tt_anv = 2
            }
        }

        val copy = BeregningRelasjon(source)

        with(copy.beregning2011 as Uforetrygdberegning) {
            bruttoPerAr shouldBe 1
            formelKodeEnum shouldBe FormelKodeEnum.AFP3
            tt_anv shouldBe 2
        }
    }
})
