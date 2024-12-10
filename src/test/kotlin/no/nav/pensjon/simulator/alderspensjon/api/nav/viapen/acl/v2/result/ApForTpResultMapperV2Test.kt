package no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.result

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.beregn.BeholdningPeriode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertAlderspensjon
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtMidnight
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import java.time.LocalDate
import java.util.Calendar

class ApForTpResultMapperV2Test : FunSpec({

    test("toApForTpResultV2 maps datoFom with appropriate time part") {
        val actual = ApForTpResultMapperV2.toApForTpResultV2(
            SimulatorOutput().apply {
                alderspensjon = SimulertAlderspensjon().apply {
                    pensjonBeholdningListe = listOf(
                        BeholdningPeriode(
                            datoFom = LocalDate.of(2024, 5, 6),
                            pensjonsbeholdning = null,
                            garantipensjonsbeholdning = null,
                            garantitilleggsbeholdning = null,
                            garantipensjonsniva = null
                        )
                    )
                    simulertBeregningInformasjonListe = listOf(
                        SimulertBeregningInformasjon().apply { datoFom = LocalDate.of(2023, 4, 5) }
                    )
                }
            })

        with(actual.ap!!) {
            pensjonsbeholdningListe.orEmpty()[0].datoFom shouldBe dateAtNoon(2024, Calendar.MAY, 6)
            simulertBeregningsinformasjonListe.orEmpty()[0].datoFom shouldBe dateAtMidnight(2023, Calendar.APRIL, 5)
        }
        /* When SimulatorOutput is data class, use this:
        ApForTpResultMapperV2.toApForTpResultV2(
            SimulatorOutput().apply {
                alderspensjon = SimulertAlderspensjon().apply {
                    pensjonBeholdningListe = listOf(
                        BeholdningPeriode(
                            datoFom = LocalDate.of(2024, 5, 6),
                            pensjonsbeholdning = null,
                            garantipensjonsbeholdning = null,
                            garantitilleggsbeholdning = null,
                            garantipensjonsniva = null
                        )
                    )
                    simulertBeregningInformasjonListe = listOf(
                        SimulertBeregningInformasjon().apply { datoFom = LocalDate.of(2023, 4, 5) }
                    )
                }
            }) shouldBe
                ApForTpResultV2(
                    ap = ApForTpAlderspensjonV2(
                        pensjonsbeholdningListe = mutableListOf(
                            ApForTpBeholdningPeriodeV2(
                                datoFom = dateAtNoon(2024, Calendar.MAY, 6)
                            )
                        ),
                        simulertBeregningsinformasjonListe = mutableListOf(
                            ApForTpBeregningInformasjonV2(
                                datoFom = dateAtMidnight(2023, Calendar.APRIL, 5)
                            )
                        )
                    )
                )
        */
    }
})
