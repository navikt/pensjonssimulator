package no.nav.pensjon.simulator.alderspensjon.api.nav.direct.anonym.acl.v1.result

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.afp.offentlig.fra2025.LivsvarigOffentligAfpOutput
import no.nav.pensjon.simulator.afp.privat.PrivatAfpPeriode
import no.nav.pensjon.simulator.core.result.PensjonPeriode
import no.nav.pensjon.simulator.core.result.SimulertAlderspensjon
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon
import no.nav.pensjon.simulator.core.result.SimulatorOutput

class AnonymSimuleringResultMapperV1Test : FunSpec({

    test("mapSimuleringResult wraps result in envelope") {
        val output = SimulatorOutput()

        val envelope = AnonymSimuleringResultMapperV1.mapSimuleringResult(output)

        envelope.result shouldBe AnonymSimuleringResultV1(
            alderspensjonAndelKapittel19 = 0.0,
            alderspensjonAndelKapittel20 = 0.0,
            alderspensjonPerioder = emptyList(),
            afpPrivatPerioder = emptyList(),
            afpOffentligPerioder = emptyList()
        )
    }

    test("mapSimuleringResult maps kapittel andeler from alderspensjon") {
        val output = SimulatorOutput().apply {
            alderspensjon = SimulertAlderspensjon().apply {
                kapittel19Andel = 0.65
                kapittel20Andel = 0.35
            }
        }

        val result = AnonymSimuleringResultMapperV1.mapSimuleringResult(output).result!!

        result.alderspensjonAndelKapittel19 shouldBe 0.65
        result.alderspensjonAndelKapittel20 shouldBe 0.35
    }

    test("mapSimuleringResult defaults kapittel andeler to 0 when alderspensjon is null") {
        val output = SimulatorOutput()

        val result = AnonymSimuleringResultMapperV1.mapSimuleringResult(output).result!!

        result.alderspensjonAndelKapittel19 shouldBe 0.0
        result.alderspensjonAndelKapittel20 shouldBe 0.0
    }

    test("mapSimuleringResult maps alderspensjon perioder") {
        val output = SimulatorOutput().apply {
            alderspensjon = SimulertAlderspensjon().apply {
                pensjonPeriodeListe.add(PensjonPeriode().apply {
                    beloep = 250000
                    alderAar = 67
                })
                pensjonPeriodeListe.add(PensjonPeriode().apply {
                    beloep = 270000
                    alderAar = 68
                })
            }
        }

        val result = AnonymSimuleringResultMapperV1.mapSimuleringResult(output).result!!

        result.alderspensjonPerioder shouldHaveSize 2
        result.alderspensjonPerioder[0].belop shouldBe 250000
        result.alderspensjonPerioder[0].alder shouldBe 67
        result.alderspensjonPerioder[1].belop shouldBe 270000
        result.alderspensjonPerioder[1].alder shouldBe 68
    }

    test("mapSimuleringResult returns empty alderspensjonPerioder when alderspensjon is null") {
        val output = SimulatorOutput()

        val result = AnonymSimuleringResultMapperV1.mapSimuleringResult(output).result!!

        result.alderspensjonPerioder.shouldBeEmpty()
    }

    test("mapSimuleringResult maps first simulertBeregningInformasjon from periode") {
        val output = SimulatorOutput().apply {
            alderspensjon = SimulertAlderspensjon().apply {
                pensjonPeriodeListe.add(PensjonPeriode().apply {
                    beloep = 200000
                    alderAar = 67
                    simulertBeregningInformasjonListe.add(SimulertBeregningInformasjon().apply {
                        spt = 4.56
                        grunnpensjon = 100000
                        tilleggspensjon = 50000
                        tt_anv_kap19 = 40
                        tt_anv_kap20 = 38
                        pa_e91 = 20
                        pa_f92 = 15
                        forholdstall = 1.05
                        delingstall = 16.5
                        pensjonBeholdningEtterUttak = 3000000
                        inntektspensjon = 180000
                        garantipensjon = 20000
                    })
                    simulertBeregningInformasjonListe.add(SimulertBeregningInformasjon().apply {
                        spt = 9.99 // should be ignored - only first is used
                    })
                })
            }
        }

        val result = AnonymSimuleringResultMapperV1.mapSimuleringResult(output).result!!
        val info = result.alderspensjonPerioder[0].simulertBeregningsinformasjon!!

        info.spt shouldBe 4.56
        info.gp shouldBe 100000
        info.tp shouldBe 50000
        info.ttAnvKap19 shouldBe 40
        info.ttAnvKap20 shouldBe 38
        info.paE91 shouldBe 20
        info.paF92 shouldBe 15
        info.forholdstall shouldBe 1.05
        info.delingstall shouldBe 16.5
        info.pensjonsbeholdningEtterUttak shouldBe 3000000
        info.inntektspensjon shouldBe 180000
        info.garantipensjon shouldBe 20000
    }

    test("mapSimuleringResult sets simulertBeregningsinformasjon to null when liste is empty") {
        val output = SimulatorOutput().apply {
            alderspensjon = SimulertAlderspensjon().apply {
                pensjonPeriodeListe.add(PensjonPeriode().apply {
                    beloep = 200000
                    alderAar = 67
                })
            }
        }

        val result = AnonymSimuleringResultMapperV1.mapSimuleringResult(output).result!!

        result.alderspensjonPerioder[0].simulertBeregningsinformasjon shouldBe null
    }

    test("mapSimuleringResult maps privat AFP perioder") {
        val output = SimulatorOutput().apply {
            privatAfpPeriodeListe.add(PrivatAfpPeriode(alderAar = 62, aarligBeloep = 60000, maanedligBeloep = 5000))
            privatAfpPeriodeListe.add(PrivatAfpPeriode(alderAar = 63, aarligBeloep = 62000, maanedligBeloep = 5167))
        }

        val result = AnonymSimuleringResultMapperV1.mapSimuleringResult(output).result!!

        result.afpPrivatPerioder shouldHaveSize 2
        result.afpPrivatPerioder[0].alder shouldBe 62
        result.afpPrivatPerioder[0].belopArlig shouldBe 60000
        result.afpPrivatPerioder[0].belopMnd shouldBe 5000
        result.afpPrivatPerioder[1].alder shouldBe 63
        result.afpPrivatPerioder[1].belopArlig shouldBe 62000
        result.afpPrivatPerioder[1].belopMnd shouldBe 5167
    }

    test("mapSimuleringResult returns empty afpPrivatPerioder when none exist") {
        val output = SimulatorOutput()

        val result = AnonymSimuleringResultMapperV1.mapSimuleringResult(output).result!!

        result.afpPrivatPerioder.shouldBeEmpty()
    }

    test("mapSimuleringResult maps offentlig AFP perioder") {
        val output = SimulatorOutput().apply {
            livsvarigOffentligAfp = listOf(
                LivsvarigOffentligAfpOutput(alderAar = 62, beloep = 48000, maanedligBeloep = 4000),
                LivsvarigOffentligAfpOutput(alderAar = 63, beloep = 50000, maanedligBeloep = 4167)
            )
        }

        val result = AnonymSimuleringResultMapperV1.mapSimuleringResult(output).result!!

        result.afpOffentligPerioder shouldHaveSize 2
        result.afpOffentligPerioder[0].alder shouldBe 62
        result.afpOffentligPerioder[0].belopArlig shouldBe 48000
        result.afpOffentligPerioder[0].belopMnd shouldBe 48000 // uses beloep, not maanedligBeloep
        result.afpOffentligPerioder[1].alder shouldBe 63
        result.afpOffentligPerioder[1].belopArlig shouldBe 50000
        result.afpOffentligPerioder[1].belopMnd shouldBe 50000
    }

    test("mapSimuleringResult returns empty afpOffentligPerioder when livsvarigOffentligAfp is null") {
        val output = SimulatorOutput().apply {
            livsvarigOffentligAfp = null
        }

        val result = AnonymSimuleringResultMapperV1.mapSimuleringResult(output).result!!

        result.afpOffentligPerioder.shouldBeEmpty()
    }

    test("mapSimuleringResult handles null fields in SimulertBeregningInformasjon") {
        val output = SimulatorOutput().apply {
            alderspensjon = SimulertAlderspensjon().apply {
                pensjonPeriodeListe.add(PensjonPeriode().apply {
                    simulertBeregningInformasjonListe.add(SimulertBeregningInformasjon())
                })
            }
        }

        val result = AnonymSimuleringResultMapperV1.mapSimuleringResult(output).result!!
        val info = result.alderspensjonPerioder[0].simulertBeregningsinformasjon!!

        info.spt shouldBe null
        info.gp shouldBe null
        info.tp shouldBe null
        info.ttAnvKap19 shouldBe null
        info.ttAnvKap20 shouldBe null
        info.paE91 shouldBe null
        info.paF92 shouldBe null
        info.forholdstall shouldBe null
        info.delingstall shouldBe null
        info.pensjonsbeholdningEtterUttak shouldBe null
        info.inntektspensjon shouldBe null
        info.garantipensjon shouldBe null
    }

    test("mapSimuleringResult handles null fields in PrivatAfpPeriode") {
        val output = SimulatorOutput().apply {
            privatAfpPeriodeListe.add(PrivatAfpPeriode())
        }

        val result = AnonymSimuleringResultMapperV1.mapSimuleringResult(output).result!!

        result.afpPrivatPerioder[0].alder shouldBe null
        result.afpPrivatPerioder[0].belopArlig shouldBe null
        result.afpPrivatPerioder[0].belopMnd shouldBe null
    }
})
