package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.offentligafp

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning
import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengrekke
import no.nav.pensjon.simulator.core.domain.regler.beregning.Sluttpoengtall
import no.nav.pensjon.simulator.core.domain.regler.beregning.Tilleggspensjon
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat

class OffentligAfpAggregatorTest : StringSpec({

    ("aggregate skal returnere null hvis ingen offentlig afp er simulert") {
        OffentligAfpAggregator.aggregate(null, true) shouldBe null
    }

    ("aggregate skal returnere null hvis type ikk er afp etterfulgt av alderspensjon") {
        val pre2025OffentligAfp = mockOffentligAfp(1, 2)
        OffentligAfpAggregator.aggregate(pre2025OffentligAfp, false) shouldBe null
    }

    ("aggregate skal returnere afp etterfulgt av alderspensjon") {
        val pre2025OffentligAfp = mockOffentligAfp(1, 2)
        val result = OffentligAfpAggregator.aggregate(pre2025OffentligAfp, true)

        result?.brutto shouldBe 1
        result?.tidligerePensjonsgivendeInntekt shouldBe 2
    }

    ("aggregate skal returnere afp etterfulgt av alderspensjon med tpi=0") {
        val pre2025OffentligAfp = mockOffentligAfp(1)
        val result = OffentligAfpAggregator.aggregate(pre2025OffentligAfp, true)

        result?.brutto shouldBe 1
        result?.tidligerePensjonsgivendeInntekt shouldBe 0
    }
})

fun mockOffentligAfp(
    brutto: Int,
    tpi: Int? = null
): Simuleringsresultat {
    return Simuleringsresultat().apply {
        beregning = Beregning()
            .apply {
                this.brutto = brutto
                tp = Tilleggspensjon().apply {
                    spt = Sluttpoengtall().apply {
                        poengrekke = Poengrekke()
                            .apply {
                                tpi?.let { this.tpi = it }
                            }
                    }
                }
            }
    }
}
