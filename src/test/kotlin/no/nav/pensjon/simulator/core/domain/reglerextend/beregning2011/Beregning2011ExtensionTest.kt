package no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatBeregning
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpOpptjening

class Beregning2011ExtensionTest : FunSpec({

    test("BeregningsResultatAfpPrivat.copy should copy BeregningsResultatAfpPrivat") {
        val original = BeregningsResultatAfpPrivat().apply {
            afpPrivatBeregning = AfpPrivatBeregning().apply {
                afpOpptjening = AfpOpptjening().apply {
                    ar = 2025
                    totalbelop = 1.2
                }
            }
        }

        with(original.copy().afpPrivatBeregning?.afpOpptjening!!) {
            beholdningsTypeEnum shouldBe BeholdningtypeEnum.AFP
            ar shouldBe 2025
            totalbelop shouldBe 1.2
        }
    }
})
