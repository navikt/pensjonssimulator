package no.nav.pensjon.simulator.core.beregn

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.beregning.AfpTillegg
import no.nav.pensjon.simulator.core.domain.regler.beregning.Familietillegg
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*

class Alderspensjon2011SisteBeregningCreatorTest : FunSpec({

    /**
     * Test filter for irrelevante ytelseskomponenter.
     * En ytelseskomponent er irrelevant hvis den er opphørt eller ubrukt.
     */
    test("createBeregning should filter out irrelevante ytelseskomponenter") {
        val beregning =
            Alderspensjon2011SisteBeregningCreator(kravService = mockk()).createBeregning(
                beregningSpec(
                    ytelseskomponentListe = mutableListOf(
                        Garantipensjon().apply {
                            brukt = true
                            opphort = false
                            brutto = 1
                        },
                        AfpTillegg().apply {
                            brukt = true
                            opphort = true // => irrelevant
                            brutto = 2
                        },
                        Familietillegg().apply {
                            brukt = false // => irrelevant
                            opphort = false
                            brutto = 3
                        },
                        Skjermingstillegg().apply {
                            brukt = true
                            opphort = false
                            brutto = 4
                        })
                ),
                BeregningsResultatAlderspensjon2011()
            ) as SisteAldersberegning2011

        with(beregning.pensjonUnderUtbetaling!!) {
            ytelseskomponenter.size shouldBe 2
            ytelseskomponenter[0].brutto shouldBe 1
            ytelseskomponenter[1].brutto shouldBe 4
        }
    }
})

private fun beregningSpec(ytelseskomponentListe: MutableList<Ytelseskomponent>) =
    SisteBeregningSpec(
        beregningsresultat = BeregningsResultatAlderspensjon2011().apply {
            pensjonUnderUtbetaling = PensjonUnderUtbetaling().apply { ytelseskomponenter = ytelseskomponentListe }
        },
        regelverkKodePaNyttKrav = null,
        forrigeKravhode = null,
        filtrertVilkarsvedtakList = emptyList(),
        isRegelverk1967 = false,
        vilkarsvedtakListe = emptyList(),
        kravhode = null,
        beregning = null,
        fomDato = null,
        tomDato = null,
        regelverk1967VirkToEarly = false
    )
