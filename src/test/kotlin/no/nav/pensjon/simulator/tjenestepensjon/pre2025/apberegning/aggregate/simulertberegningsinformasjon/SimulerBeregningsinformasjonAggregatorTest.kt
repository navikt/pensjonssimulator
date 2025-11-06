package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.simulertberegningsinformasjon

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon
import java.time.LocalDate

class SimulerBeregningsinformasjonAggregatorTest : StringSpec({

    ("Ingen simuleringsinformasjon skal bli en tom liste") {
        val list = listOf<SimulertBeregningInformasjon>()
        SimulerBeregningsinformasjonAggregator.aggregate(
            foedselsdato = LocalDate.of(1960, 2, 2),
            foersteUttakDato = LocalDate.of(2027, 3, 1),
            simulertBeregningInformasjonListe = list
        ) shouldBe emptyList()
    }

    ("aggragate skal returnere en tom liste når ingen alderspensjon er beregnet") {
        SimulerBeregningsinformasjonAggregator.aggregate(
            foedselsdato = LocalDate.of(1960, 2, 2),
            foersteUttakDato = LocalDate.of(2027, 3, 1),
            simulertBeregningInformasjonListe = null
        ) shouldBe emptyList()
    }

    ("aggregate skal fylle alle felter i simuleringsdata") {
        val list = listOf(
            SimulertBeregningInformasjon().apply {
                datoFom = LocalDate.of(2027, 3, 1)
                tt_anv_kap19 = 1
                pa_f92 = 2
                pa_e91 = 3
                ufoereGrad = 4
                basisGrunnpensjon = 5.0
                basisPensjonstillegg = 6.0
                basisTilleggspensjon = 7.0
                delingstall = 8.0
                forholdstall = 9.0
                spt = 10.0
            },
        )
        val result = SimulerBeregningsinformasjonAggregator.aggregate(
            foedselsdato = LocalDate.of(1960, 2, 2),
            foersteUttakDato = LocalDate.of(2027, 3, 1),
            simulertBeregningInformasjonListe = list
        )
        result.size shouldBe 1
        with(result[0]) {
            fom shouldBe LocalDate.of(2027, 3, 1)
            andvendtTrygdetid shouldBe 1
            poengAarTom1991 shouldBe 2
            poengAarFom1992 shouldBe 3
            ufoeregradVedOmregning shouldBe 4
            basisGrunnpensjon shouldBe 5.0
            basisPensjonstillegg shouldBe 6.0
            basisTilleggspensjon shouldBe 7.0
            delingstallUttak shouldBe 8.0
            forholdstallUttak shouldBe 9.0
            sluttpoengtall shouldBe 10.0
        }
    }

    ("aggregate skal sette null eller default verdi i feltene til simuleringsdata") {
        val list = listOf(
            SimulertBeregningInformasjon().apply {
                datoFom = LocalDate.of(2027, 3, 1)
                tt_anv_kap19 = 1
                pa_f92 = 2
                pa_e91 = 3
                ufoereGrad = null
                basisGrunnpensjon = null
                basisPensjonstillegg = null
                basisTilleggspensjon = null
                delingstall = 8.0
                forholdstall = 9.0
                spt = null
            },
        )
        val result = SimulerBeregningsinformasjonAggregator.aggregate(
            foedselsdato = LocalDate.of(1960, 2, 2),
            foersteUttakDato = LocalDate.of(2027, 3, 1),
            simulertBeregningInformasjonListe = list
        )
        result.size shouldBe 1
        with(result[0]) {
            fom shouldBe LocalDate.of(2027, 3, 1)
            andvendtTrygdetid shouldBe 1
            poengAarTom1991 shouldBe 2
            poengAarFom1992 shouldBe 3
            ufoeregradVedOmregning shouldBe 0
            basisGrunnpensjon shouldBe null
            basisPensjonstillegg shouldBe null
            basisTilleggspensjon shouldBe null
            delingstallUttak shouldBe 8.0
            forholdstallUttak shouldBe 9.0
            sluttpoengtall shouldBe null
        }
    }

    ("aggregate skal returnere beregningsinformasjonsperioder senere enn brukers fylte 67 år") {
        val list = listOf(
            SimulertBeregningInformasjon().apply {
                datoFom = LocalDate.of(2030, 3, 1)
                tt_anv_kap19 = 1
                pa_f92 = 2
                pa_e91 = 3
                ufoereGrad = 4
                basisGrunnpensjon = 5.0
                basisPensjonstillegg = 6.0
                basisTilleggspensjon = 7.0
                delingstall = 8.0
                forholdstall = 9.0
                spt = 10.0
            },
        )
        val result = SimulerBeregningsinformasjonAggregator.aggregate(
            foedselsdato = LocalDate.of(1960, 2, 2),
            foersteUttakDato = LocalDate.of(2025, 3, 1),
            simulertBeregningInformasjonListe = list
        )
        result.size shouldBe 1
        with(result[0]) {
            fom shouldBe LocalDate.of(2030, 3, 1)
        }
    }

    ("aggregate skal returnere beregningsinformasjonsperioder senere enn første uttaksdato") {
        val list = listOf(
            SimulertBeregningInformasjon().apply {
                datoFom = LocalDate.of(2030, 3, 1)
                tt_anv_kap19 = 1
                pa_f92 = 2
                pa_e91 = 3
                ufoereGrad = 4
                basisGrunnpensjon = 5.0
                basisPensjonstillegg = 6.0
                basisTilleggspensjon = 7.0
                delingstall = 8.0
                forholdstall = 9.0
                spt = 10.0
            },
            SimulertBeregningInformasjon().apply {
                datoFom = LocalDate.of(2025, 3, 1)
                tt_anv_kap19 = 11
                pa_f92 = 12
                pa_e91 = 13
                ufoereGrad = 14
                basisGrunnpensjon = 15.0
                basisPensjonstillegg = 16.0
                basisTilleggspensjon = 17.0
                delingstall = 18.0
                forholdstall = 19.0
                spt = 20.0
            },
        )
        val result = SimulerBeregningsinformasjonAggregator.aggregate(
            foedselsdato = LocalDate.of(1960, 2, 2),
            foersteUttakDato = LocalDate.of(2029, 3, 1),
            simulertBeregningInformasjonListe = list
        )
        result.size shouldBe 1
        with(result[0]) {
            fom shouldBe LocalDate.of(2030, 3, 1)
        }
    }

})
