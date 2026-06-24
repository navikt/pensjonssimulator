package no.nav.pensjon.simulator.alderspensjon.convert

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertMaanedligAlderspensjon
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertMaanedligGarantipensjon
import no.nav.pensjon.simulator.core.result.PensjonPeriode
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon
import java.time.LocalDate

open class MaanedligAlderspensjonBeregnerTest : ShouldSpec({

    should("gi simulert månedlig alderspensjon") {
        val fom = LocalDate.of(2030, 1, 1)

        MaanedligAlderspensjonBeregner(
            beregningsinfoListe = beregningsinformasjonListe(fom),
            periodeListe = periodeListe(fom, beholdningFoerUttak = 1234),
            kapittel19Andel = 0.3,
            kapittel20Andel = 0.7
        ).maanedligAlderspensjon(fom) shouldBe
                SimulertMaanedligAlderspensjon(
                    beloep = 5000,
                    inntektspensjon = 1000,
                    delingstall = 14.5,
                    pensjonBeholdningFoerUttak = 1234,
                    pensjonBeholdningEtterUttak = 500000,
                    sluttpoengtall = 3.5,
                    poengaarFoer92 = 10,
                    poengaarEtter91 = 15,
                    forholdstall = 1.1,
                    grunnpensjon = 800,
                    tilleggspensjon = 900,
                    pensjonstillegg = 200,
                    skjermingstillegg = 50,
                    andelsbroekKap19 = 0.3,
                    andelsbroekKap20 = 0.7,
                    basispensjon = 1200,
                    restpensjon = 300,
                    gjenlevendetillegg = 100,
                    minstePensjonsnivaaSats = 2.5,
                    trygdetidKap19 = 40,
                    trygdetidKap20 = 20,
                    garantipensjon = SimulertMaanedligGarantipensjon(maanedligBeloep = 600, sats = 2.0),
                    garantitillegg = 50
                )
    }

    should("hente garantipensjonssats fra periodelisten hvis beregningsinfo mangler den verdien") {
        val fom = LocalDate.of(2030, 1, 1)

        MaanedligAlderspensjonBeregner(
            beregningsinfoListe = beregningsinformasjonListe(fom, garantipensjonssats = null),
            periodeListe = periodeListe(fom, garantipensjonssats = 2.1),
            kapittel19Andel = 0.3,
            kapittel20Andel = 0.7
        ).maanedligAlderspensjon(fom)?.garantipensjon?.sats shouldBe 2.1
    }
})

private fun beregningsinformasjonListe(
    fom: LocalDate,
    garantipensjonssats: Double? = 2.0
): List<SimulertBeregningInformasjon> =
    listOf(
        SimulertBeregningInformasjon().apply {
            datoFom = fom
            maanedligBeloep = 5000
            inntektspensjonPerMaaned = 1000
            delingstall = 14.5
            pensjonBeholdningEtterUttak = 500000
            spt = 3.5
            pa_f92 = 10
            pa_e91 = 15
            forholdstall = 1.1
            grunnpensjonPerMaaned = 800
            tilleggspensjonPerMaaned = 900
            pensjonstilleggPerMaaned = 200
            skjermingstillegg = 500
            skjermingstilleggPerMaaned = 50
            basispensjon = 1200
            restBasisPensjon = 300
            gjtAPKap19PerMaaned = 100
            minstePensjonsnivaSats = 2.5
            tt_anv_kap19 = 40
            tt_anv_kap20 = 20
            garantipensjonPerMaaned = 600
            this.garantipensjonssats = garantipensjonssats
            garantitilleggPerMaaned = 50
        })

private fun periodeListe(
    fom: LocalDate,
    garantipensjonssats: Double? = null,
    beholdningFoerUttak: Int? = null
): List<PensjonPeriode> =
    listOf(
        PensjonPeriode().apply {
            simulertBeregningInformasjonListe = mutableListOf(
                SimulertBeregningInformasjon().apply {
                    datoFom = fom
                    this.garantipensjonssats = garantipensjonssats
                    pensjonBeholdningFoerUttak = beholdningFoerUttak
                })
        })