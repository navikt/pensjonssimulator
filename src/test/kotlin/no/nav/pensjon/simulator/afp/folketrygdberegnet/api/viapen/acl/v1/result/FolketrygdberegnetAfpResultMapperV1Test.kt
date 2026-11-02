package no.nav.pensjon.simulator.afp.folketrygdberegnet.api.viapen.acl.v1.result

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.beregning.*
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import java.time.LocalDate
import java.util.*

class FolketrygdberegnetAfpResultMapperV1Test : FunSpec({

    test("toResultV1 returns null when tidsbegrensetOffentligAfp is null") {
        FolketrygdberegnetAfpResultMapperV1.toResultV1(SimulatorOutput()) shouldBe null
    }

    test("toResultV1 returns null when beregning is null") {
        val output = SimulatorOutput(tidsbegrensetOffentligAfp = Simuleringsresultat())
        FolketrygdberegnetAfpResultMapperV1.toResultV1(output) shouldBe null
    }

    test("toResultV1 maps all fields from fully populated beregning") {
        val fpp = FramtidigPensjonspoengtall().apply { pt = 4.5 }
        val poengrekke = Poengrekke().apply {
            tpi = 350000
            pa = 30
            pa_f92 = 10
            pa_e91 = 20
            this.fpp = fpp
        }
        val sluttpoengtall = Sluttpoengtall().apply {
            pt = 5.67
            this.poengrekke = poengrekke
        }
        val tilleggspensjon = Tilleggspensjon().apply {
            spt = sluttpoengtall
            netto = 12000
        }
        val beregning = Beregning().apply {
            netto = 25000
            virkFomLd = LocalDate.of(2029, 1, 1)
            tt_anv = 40
            g = 124028
            tp = tilleggspensjon
            gp = Grunnpensjon().apply { netto = 8000 }
            afpTillegg = AfpTillegg().apply { netto = 3000 }
            st = Sertillegg().apply { netto = 2000 }
        }

        val result = FolketrygdberegnetAfpResultMapperV1.toResultV1(output(beregning))!!

        with(result) {
            totalbelopAfp shouldBe 25000
            trygdetid shouldBe 40
            grunnbelop shouldBe 124028
            tidligereArbeidsinntekt shouldBe 350000
            this.sluttpoengtall shouldBe 5.67
            poengar shouldBe 30
            poeangar_f92 shouldBe 10
            poeangar_e91 shouldBe 20
            this.tilleggspensjon shouldBe 12000
            this.fpp shouldBe 4.5
            grunnpensjon shouldBe 8000
            afpTillegg shouldBe 3000
            sertillegg shouldBe 2000
        }
    }

    test("toResultV1 converts virkFom to Norwegian noon") {
        val beregning = Beregning().apply { virkFomLd = LocalDate.of(2029, 6, 15) }

        val result = FolketrygdberegnetAfpResultMapperV1.toResultV1(output(beregning))!!

        val cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo"))
        cal.time = result.virkFom!!
        cal[Calendar.YEAR] shouldBe 2029
        cal[Calendar.MONTH] shouldBe Calendar.JUNE
        cal[Calendar.DAY_OF_MONTH] shouldBe 15
        cal[Calendar.HOUR_OF_DAY] shouldBe 12
    }

    test("toResultV1 sets virkFom to null when beregning virkFom is null") {
        val beregning = Beregning().apply { virkFomLd = null }
        FolketrygdberegnetAfpResultMapperV1.toResultV1(output(beregning))!!.virkFom shouldBe null
    }

    test("toResultV1 handles null tilleggspensjon") {
        val beregning = Beregning().apply { tp = null }

        val result = FolketrygdberegnetAfpResultMapperV1.toResultV1(output(beregning))

        with(result!!) {
            sluttpoengtall shouldBe null
            tidligereArbeidsinntekt shouldBe null
            poengar shouldBe null
            poeangar_f92 shouldBe null
            poeangar_e91 shouldBe null
            tilleggspensjon shouldBe null
            fpp shouldBe null
        }
    }

    test("toResultV1 handles null sluttpoengtall in tilleggspensjon") {
        val beregning = Beregning().apply {
            tp = Tilleggspensjon().apply {
                spt = null
                netto = 5000
            }
        }

        val result = FolketrygdberegnetAfpResultMapperV1.toResultV1(output(beregning))

        with(result!!) {
            sluttpoengtall shouldBe null
            tidligereArbeidsinntekt shouldBe null
            poengar shouldBe null
            tilleggspensjon shouldBe 5000
            fpp shouldBe null
        }
    }

    test("toResultV1 handles null poengrekke in sluttpoengtall") {
        val beregning = Beregning().apply {
            tp = Tilleggspensjon().apply {
                spt = Sluttpoengtall().apply { pt = 3.45; poengrekke = null }
            }
        }

        val result = FolketrygdberegnetAfpResultMapperV1.toResultV1(output(beregning))

        with(result!!) {
            sluttpoengtall shouldBe 3.45
            tidligereArbeidsinntekt shouldBe null
            poengar shouldBe null
            fpp shouldBe null
        }
    }

    test("toResultV1 handles null fpp in poengrekke") {
        val beregning = Beregning().apply {
            tp = Tilleggspensjon().apply {
                spt = Sluttpoengtall().apply {
                    poengrekke = Poengrekke().apply { fpp = null; tpi = 100000 }
                }
            }
        }

        val result = FolketrygdberegnetAfpResultMapperV1.toResultV1(output(beregning))

        with(result!!) {
            fpp shouldBe null
            tidligereArbeidsinntekt shouldBe 100000
        }
    }

    test("toResultV1 handles null grunnpensjon, afpTillegg, and sertillegg") {
        val beregning = Beregning().apply {
            gp = null
            afpTillegg = null
            st = null
        }

        val result = FolketrygdberegnetAfpResultMapperV1.toResultV1(output(beregning))

        with(result!!) {
            grunnpensjon shouldBe null
            afpTillegg shouldBe null
            sertillegg shouldBe null
        }
    }

    test("toResultV1 uses default zero values for netto, tt_anv, and g on empty beregning") {
        val result = FolketrygdberegnetAfpResultMapperV1.toResultV1(output(Beregning()))

        with(result!!) {
            totalbelopAfp shouldBe 0
            trygdetid shouldBe 0
            grunnbelop shouldBe 0
        }
    }
})

private fun output(beregning: Beregning) =
    SimulatorOutput(
        tidsbegrensetOffentligAfp = Simuleringsresultat().apply { this.beregning = beregning }
    )