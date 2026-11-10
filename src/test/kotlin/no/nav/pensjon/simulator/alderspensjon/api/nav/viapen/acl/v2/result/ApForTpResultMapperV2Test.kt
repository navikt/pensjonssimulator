package no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.result

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.beregn.BeholdningPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.SivilstandEnum
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertAlderspensjon
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon
import no.nav.pensjon.simulator.core.util.toNorwegianDate
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import java.time.LocalDate

class ApForTpResultMapperV2Test : ShouldSpec({

    should("map datoFom with appropriate time part") {
        ApForTpResultMapperV2.toApForTpResultV2(
            SimulatorOutput(
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
            )
        ) shouldBe ApForTpResultV2(
            ap = ApForTpAlderspensjonV2(
                pensjonsbeholdningListe = mutableListOf(
                    ApForTpBeholdningPeriodeV2(
                        datoFom = LocalDate.of(2024, 5, 6).toNorwegianDateAtNoon()
                    )
                ),
                simulertBeregningsinformasjonListe = mutableListOf(
                    ApForTpBeregningInformasjonV2(
                        datoFom = LocalDate.of(2023, 4, 5).toNorwegianDate(),
                    )
                )
            ),
            sivilstand = SivilstandEnum.NULL
        )
    }
})