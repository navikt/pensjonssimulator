package no.nav.pensjon.simulator.afp.folketrygdberegnet.api.direct.acl.v0.result

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.beregning.*
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import java.util.*

class TpoFolketrygdberegnetAfpResultMapperV0Test : FunSpec({

    test("toResultV0 returns null when pre2025OffentligAfp is null") {
        val output = SimulatorOutput()

        val result = TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(output)

        result shouldBe null
    }

    test("toResultV0 returns null when beregning is null") {
        val output = SimulatorOutput().apply {
            pre2025OffentligAfp = Simuleringsresultat(beregning = null)
        }

        val result = TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(output)

        result shouldBe null
    }

    test("toResultV0 maps all fields from fully populated beregning") {
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
        val virkFomDate = dateAtNoon(2029, Calendar.JANUARY, 1)
        val beregning = Beregning().apply {
            netto = 25000
            virkFom = virkFomDate
            tt_anv = 40
            g = 124028
            tp = tilleggspensjon
            gp = Grunnpensjon().apply { netto = 8000 }
            afpTillegg = AfpTillegg().apply { netto = 3000 }
            st = Sertillegg().apply { netto = 2000 }
        }
        val output = SimulatorOutput().apply {
            pre2025OffentligAfp = Simuleringsresultat(beregning = beregning)
        }

        val result = TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(output)!!

        result.totalbelopAfp shouldBe 25000
        result.trygdetid shouldBe 40
        result.grunnbelop shouldBe 124028
        result.tidligereArbeidsinntekt shouldBe 350000
        result.sluttpoengtall shouldBe 5.67
        result.poengar shouldBe 30
        result.poeangar_f92 shouldBe 10
        result.poeangar_e91 shouldBe 20
        result.tilleggspensjon shouldBe 12000
        result.fpp shouldBe 4.5
        result.grunnpensjon shouldBe 8000
        result.afpTillegg shouldBe 3000
        result.sertillegg shouldBe 2000
    }

    test("toResultV0 converts virkFom to Norwegian noon") {
        val virkFomDate = dateAtNoon(2029, Calendar.JUNE, 15)
        val beregning = Beregning().apply { virkFom = virkFomDate }
        val output = SimulatorOutput().apply {
            pre2025OffentligAfp = Simuleringsresultat(beregning = beregning)
        }

        val result = TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(output)!!

        val cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo"))
        cal.time = result.virkFom!!
        cal[Calendar.YEAR] shouldBe 2029
        cal[Calendar.MONTH] shouldBe Calendar.JUNE
        cal[Calendar.DAY_OF_MONTH] shouldBe 15
        cal[Calendar.HOUR_OF_DAY] shouldBe 12
    }

    test("toResultV0 sets virkFom to null when beregning virkFom is null") {
        val beregning = Beregning().apply { virkFom = null }
        val output = SimulatorOutput().apply {
            pre2025OffentligAfp = Simuleringsresultat(beregning = beregning)
        }

        val result = TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(output)!!

        result.virkFom shouldBe null
    }

    test("toResultV0 handles null tilleggspensjon") {
        val beregning = Beregning().apply { tp = null }
        val output = SimulatorOutput().apply {
            pre2025OffentligAfp = Simuleringsresultat(beregning = beregning)
        }

        val result = TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(output)!!

        result.sluttpoengtall shouldBe null
        result.tidligereArbeidsinntekt shouldBe null
        result.poengar shouldBe null
        result.poeangar_f92 shouldBe null
        result.poeangar_e91 shouldBe null
        result.tilleggspensjon shouldBe null
        result.fpp shouldBe null
    }

    test("toResultV0 handles null sluttpoengtall in tilleggspensjon") {
        val beregning = Beregning().apply {
            tp = Tilleggspensjon().apply {
                spt = null
                netto = 5000
            }
        }
        val output = SimulatorOutput().apply {
            pre2025OffentligAfp = Simuleringsresultat(beregning = beregning)
        }

        val result = TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(output)!!

        result.sluttpoengtall shouldBe null
        result.tidligereArbeidsinntekt shouldBe null
        result.poengar shouldBe null
        result.poeangar_f92 shouldBe null
        result.poeangar_e91 shouldBe null
        result.tilleggspensjon shouldBe 5000
        result.fpp shouldBe null
    }

    test("toResultV0 handles null poengrekke in sluttpoengtall") {
        val beregning = Beregning().apply {
            tp = Tilleggspensjon().apply {
                spt = Sluttpoengtall().apply {
                    pt = 3.45
                    poengrekke = null
                }
            }
        }
        val output = SimulatorOutput().apply {
            pre2025OffentligAfp = Simuleringsresultat(beregning = beregning)
        }

        val result = TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(output)!!

        result.sluttpoengtall shouldBe 3.45
        result.tidligereArbeidsinntekt shouldBe null
        result.poengar shouldBe null
        result.poeangar_f92 shouldBe null
        result.poeangar_e91 shouldBe null
        result.fpp shouldBe null
    }

    test("toResultV0 handles null fpp in poengrekke") {
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
            pre2025OffentligAfp = Simuleringsresultat(beregning = beregning)
        }

        val result = TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(output)!!

        result.fpp shouldBe null
        result.tidligereArbeidsinntekt shouldBe 100000
    }

    test("toResultV0 handles null grunnpensjon") {
        val beregning = Beregning().apply { gp = null }
        val output = SimulatorOutput().apply {
            pre2025OffentligAfp = Simuleringsresultat(beregning = beregning)
        }

        val result = TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(output)!!

        result.grunnpensjon shouldBe null
    }

    test("toResultV0 handles null afpTillegg") {
        val beregning = Beregning().apply { afpTillegg = null }
        val output = SimulatorOutput().apply {
            pre2025OffentligAfp = Simuleringsresultat(beregning = beregning)
        }

        val result = TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(output)!!

        result.afpTillegg shouldBe null
    }

    test("toResultV0 handles null sertillegg") {
        val beregning = Beregning().apply { st = null }
        val output = SimulatorOutput().apply {
            pre2025OffentligAfp = Simuleringsresultat(beregning = beregning)
        }

        val result = TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(output)!!

        result.sertillegg shouldBe null
    }

    test("toResultV0 uses default zero values for netto, tt_anv, and g on empty beregning") {
        val beregning = Beregning()
        val output = SimulatorOutput().apply {
            pre2025OffentligAfp = Simuleringsresultat(beregning = beregning)
        }

        val result = TpoFolketrygdberegnetAfpResultMapperV0.toResultV0(output)!!

        result.totalbelopAfp shouldBe 0
        result.trygdetid shouldBe 0
        result.grunnbelop shouldBe 0
    }
})
