package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.Brok

class PensjonUnderUtbetalingTest : FunSpec({

    test("copy constructor copies all fields") {
        val source = PensjonUnderUtbetaling().apply {
            totalbelopNetto = 123

            ytelseskomponenter.add(
                Inntektspensjon().apply {
                    eksportBrok = Brok().apply {
                        teller = 2
                        nevner = 3
                    }
                    brutto = 456
                })
        }

        val copy = PensjonUnderUtbetaling(source, excludeBrutto = false)

        copy.totalbelopNetto shouldBe 123
        copy.inntektspensjon?.eksportBrok?.teller shouldBe 2
        copy.inntektspensjon?.eksportBrok?.nevner shouldBe 3
        copy.inntektspensjon?.brutto shouldBe 456
    }
})
