package no.nav.pensjon.simulator.afp.folketrygdberegnet.api.direct.acl.v0.result

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.beregning.*
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import java.time.LocalDate
import java.util.*

class TpoFolketrygdberegnetAfpResultMapperV0Test : ShouldSpec({

    should("return null when tidsbegrenset offentlig AFP is null") {
        TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(SimulatorOutput()) shouldBe null
    }

    should("return null when beregning is null") {
        val output = SimulatorOutput().apply {
            tidsbegrensetOffentligAfp = Simuleringsresultat()
        }

        TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(output) shouldBe null
    }

    should("map all fields from fully populated beregning") {
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
        val virkFomDate = LocalDate.of(2029, 1, 1)
        val beregning = Beregning().apply {
            netto = 25000
            virkFomLd = virkFomDate
            tt_anv = 40
            g = 124028
            tp = tilleggspensjon
            gp = Grunnpensjon().apply { netto = 8000 }
            afpTillegg = AfpTillegg().apply { netto = 3000 }
            st = Sertillegg().apply { netto = 2000 }
        }
        val output = SimulatorOutput().apply {
            tidsbegrensetOffentligAfp = Simuleringsresultat().apply { this.beregning = beregning }
        }

        val result = TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(output)

        with(result!!) {
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

    should("convert virkFom to Norwegian noon") {
        val virkFomDate = LocalDate.of(2029, 6, 15)
        val beregning = Beregning().apply { virkFomLd = virkFomDate }
        val output = SimulatorOutput().apply {
            tidsbegrensetOffentligAfp = Simuleringsresultat().apply { this.beregning = beregning }
        }

        val result = TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(output)

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo"))
        calendar.time = result!!.virkFom!!
        calendar[Calendar.YEAR] shouldBe 2029
        calendar[Calendar.MONTH] shouldBe Calendar.JUNE
        calendar[Calendar.DAY_OF_MONTH] shouldBe 15
        calendar[Calendar.HOUR_OF_DAY] shouldBe 12
    }

    should("set virkFom to null when beregning virkFom is null") {
        val beregning = Beregning().apply { virkFomLd = null }
        val output = SimulatorOutput().apply {
            tidsbegrensetOffentligAfp = Simuleringsresultat().apply { this.beregning = beregning }
        }

        TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(output)!!.virkFom shouldBe null
    }

    should("handle null tilleggspensjon") {
        val beregning = Beregning().apply { tp = null }
        val output = SimulatorOutput().apply {
            tidsbegrensetOffentligAfp = Simuleringsresultat().apply { this.beregning = beregning }
        }

        val result = TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(output)

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

    should("handle null sluttpoengtall in tilleggspensjon") {
        val beregning = Beregning().apply {
            tp = Tilleggspensjon().apply {
                spt = null
                netto = 5000
            }
        }
        val output = SimulatorOutput().apply {
            tidsbegrensetOffentligAfp = Simuleringsresultat().apply { this.beregning = beregning }
        }

        val result = TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(output)

        with(result!!) {
            sluttpoengtall shouldBe null
            tidligereArbeidsinntekt shouldBe null
            poengar shouldBe null
            poeangar_f92 shouldBe null
            poeangar_e91 shouldBe null
            tilleggspensjon shouldBe 5000
            fpp shouldBe null
        }
    }

    should("handle null poengrekke in sluttpoengtall") {
        val beregning = Beregning().apply {
            tp = Tilleggspensjon().apply {
                spt = Sluttpoengtall().apply {
                    pt = 3.45
                    poengrekke = null
                }
            }
        }
        val output = SimulatorOutput().apply {
            tidsbegrensetOffentligAfp = Simuleringsresultat().apply { this.beregning = beregning }
        }

        val result = TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(output)

        with(result!!) {
            sluttpoengtall shouldBe 3.45
            tidligereArbeidsinntekt shouldBe null
            poengar shouldBe null
            poeangar_f92 shouldBe null
            poeangar_e91 shouldBe null
            fpp shouldBe null
        }
    }

    should("handle null fpp in poengrekke") {
        val beregning = Beregning().apply {
            tp = Tilleggspensjon().apply {
                spt = Sluttpoengtall().apply {
                    poengrekke = Poengrekke().apply {
                        fpp = null
                        tpi = 100000
                    }
                }
            }
        }
        val output = SimulatorOutput().apply {
            tidsbegrensetOffentligAfp = Simuleringsresultat().apply { this.beregning = beregning }
        }

        val result = TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(output)

        with(result!!) {
            fpp shouldBe null
            tidligereArbeidsinntekt shouldBe 100000
        }
    }

    should("handle null grunnpensjon") {
        val beregning = Beregning().apply { gp = null }
        val output = SimulatorOutput().apply {
            tidsbegrensetOffentligAfp = Simuleringsresultat().apply { this.beregning = beregning }
        }

        TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(output)!!.grunnpensjon shouldBe null
    }

    should("handle null AFP-tillegg") {
        val beregning = Beregning().apply { afpTillegg = null }
        val output = SimulatorOutput().apply {
            tidsbegrensetOffentligAfp = Simuleringsresultat().apply { this.beregning = beregning }
        }

        TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(output)!!.afpTillegg shouldBe null
    }

    should("handle null særtillegg") {
        val beregning = Beregning().apply { st = null }
        val output = SimulatorOutput().apply {
            tidsbegrensetOffentligAfp = Simuleringsresultat().apply { this.beregning = beregning }
        }

        TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(output)!!.sertillegg shouldBe null
    }

    should("use default zero values for netto, tt_anv, and g on empty beregning") {
        val output = SimulatorOutput().apply {
            tidsbegrensetOffentligAfp = Simuleringsresultat().apply { beregning = Beregning() }
        }

        val result = TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(output)

        with(result!!) {
            totalbelopAfp shouldBe 0
            trygdetid shouldBe 0
            grunnbelop shouldBe 0
        }
    }
})