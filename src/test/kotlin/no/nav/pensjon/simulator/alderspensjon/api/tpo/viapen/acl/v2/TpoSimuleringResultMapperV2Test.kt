package no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v2

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.beregn.BeholdningPeriode
import no.nav.pensjon.simulator.core.beregn.GarantipensjonNivaa
import no.nav.pensjon.simulator.core.result.PensjonPeriode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertAlderspensjon
import java.time.LocalDate

class TpoSimuleringResultMapperV2Test : FunSpec({

    test("toDto should map selected values") {
        TpoSimuleringResultMapperV2.toDto(SimulatorOutput().apply {
            alderspensjon = SimulertAlderspensjon().apply {
                addPensjonsperiode(PensjonPeriode().apply {
                    alderAar = 1
                    beloep = 2
                    simulertBeregningInformasjonListe = mutableListOf()
                })
                pensjonBeholdningListe = listOf(
                    BeholdningPeriode(
                        datoFom = LocalDate.of(2024, 5, 6),
                        pensjonsbeholdning = 1.1,
                        garantipensjonsbeholdning = 2.2,
                        garantitilleggsbeholdning = 3.3,
                        garantipensjonsniva = GarantipensjonNivaa(
                            beloep = 4.4,
                            satsType = "ORDINAER",
                            sats = 5.5,
                            anvendtTrygdetid = 6
                        )
                    )
                )
            }
        }) shouldBe TpoSimuleringResultV2(
            alderspensjon = TpoAlderspensjonV2(
                pensjonsperiodeListe = listOf(
                    TpoPensjonPeriodeV2(
                        alderAar = 1,
                        aarligBeloep = 2,
                        beregningInformasjonListe = emptyList()
                    )
                ),
                pensjonsbeholdningListe = listOf(
                    TpoPensjonBeholdningPeriodeV2(
                        datoFom = LocalDate.of(2024, 5, 6),
                        pensjonsbeholdning = 1.1,
                        garantipensjonsbeholdning = 2.2,
                        garantitilleggsbeholdning = 3.3
                    )
                )
            )
        )
    }
})
