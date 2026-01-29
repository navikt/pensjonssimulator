package no.nav.pensjon.simulator.trygdetid

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import java.time.LocalDate

class InngangOgEksportGrunnlagFactoryTest : FunSpec({

    context("newInngangOgEksportGrunnlagForSimuleringUtland") {

        test("setter alltid eksportforbud til null og fortsattMedlemFT til true") {
            val persongrunnlag = Persongrunnlag()
            val kravhode = Kravhode().apply {
                regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_OPPTJ
            }

            val result = InngangOgEksportGrunnlagFactory.newInngangOgEksportGrunnlagForSimuleringUtland(
                persongrunnlag, kravhode
            )

            result.eksportforbud shouldBe null
            result.fortsattMedlemFT shouldBe true
        }

        context("for alderspensjon 2011 (N_REG_G_OPPTJ - kun kapittel 19)") {

            test("setter treArTrygdetidNorge til true når trygdetid er minst 1 år") {
                val persongrunnlag = Persongrunnlag().apply {
                    trygdetidPerioder = mutableListOf(
                        norskTrygdetidPeriode(
                            fom = LocalDate.of(2000, 1, 1),
                            tom = LocalDate.of(2002, 1, 1) // 2 år
                        )
                    )
                }
                val kravhode = Kravhode().apply {
                    regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_OPPTJ
                }

                val result = InngangOgEksportGrunnlagFactory.newInngangOgEksportGrunnlagForSimuleringUtland(
                    persongrunnlag, kravhode
                )

                result.treArTrygdetidNorge shouldBe true
                result.treArTrygdetidNorgeKap20 shouldBe null // Ikke satt for 2011
            }

            test("setter treArTrygdetidNorge til false når trygdetid er under 1 år") {
                val persongrunnlag = Persongrunnlag().apply {
                    trygdetidPerioder = mutableListOf(
                        norskTrygdetidPeriode(
                            fom = LocalDate.of(2000, 1, 1),
                            tom = LocalDate.of(2000, 6, 1) // 5 måneder
                        )
                    )
                }
                val kravhode = Kravhode().apply {
                    regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_OPPTJ
                }

                val result = InngangOgEksportGrunnlagFactory.newInngangOgEksportGrunnlagForSimuleringUtland(
                    persongrunnlag, kravhode
                )

                result.treArTrygdetidNorge shouldBe false
            }

            test("setter treArTrygdetidNorge til false når ingen trygdetidperioder") {
                val persongrunnlag = Persongrunnlag().apply {
                    trygdetidPerioder = mutableListOf()
                }
                val kravhode = Kravhode().apply {
                    regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_OPPTJ
                }

                val result = InngangOgEksportGrunnlagFactory.newInngangOgEksportGrunnlagForSimuleringUtland(
                    persongrunnlag, kravhode
                )

                result.treArTrygdetidNorge shouldBe false
            }
        }

        context("for alderspensjon 2016 (N_REG_G_N_OPPTJ - kapittel 19 og 20)") {

            test("setter treArTrygdetidNorge og treArTrygdetidNorgeKap20 til true når begge har minst 1 år") {
                val persongrunnlag = Persongrunnlag().apply {
                    trygdetidPerioder = mutableListOf(
                        norskTrygdetidPeriode(
                            fom = LocalDate.of(2000, 1, 1),
                            tom = LocalDate.of(2003, 1, 1) // 3 år
                        )
                    )
                    trygdetidPerioderKapittel20 = mutableListOf(
                        norskTrygdetidPeriode(
                            fom = LocalDate.of(2010, 1, 1),
                            tom = LocalDate.of(2012, 1, 1) // 2 år
                        )
                    )
                }
                val kravhode = Kravhode().apply {
                    regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_N_OPPTJ
                }

                val result = InngangOgEksportGrunnlagFactory.newInngangOgEksportGrunnlagForSimuleringUtland(
                    persongrunnlag, kravhode
                )

                result.treArTrygdetidNorge shouldBe true
                result.treArTrygdetidNorgeKap20 shouldBe true
            }

            test("setter treArTrygdetidNorge til true og treArTrygdetidNorgeKap20 til false") {
                val persongrunnlag = Persongrunnlag().apply {
                    trygdetidPerioder = mutableListOf(
                        norskTrygdetidPeriode(
                            fom = LocalDate.of(2000, 1, 1),
                            tom = LocalDate.of(2002, 1, 1) // 2 år
                        )
                    )
                    trygdetidPerioderKapittel20 = mutableListOf(
                        norskTrygdetidPeriode(
                            fom = LocalDate.of(2010, 1, 1),
                            tom = LocalDate.of(2010, 6, 1) // 5 måneder
                        )
                    )
                }
                val kravhode = Kravhode().apply {
                    regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_N_OPPTJ
                }

                val result = InngangOgEksportGrunnlagFactory.newInngangOgEksportGrunnlagForSimuleringUtland(
                    persongrunnlag, kravhode
                )

                result.treArTrygdetidNorge shouldBe true
                result.treArTrygdetidNorgeKap20 shouldBe false
            }

            test("setter treArTrygdetidNorge til false og treArTrygdetidNorgeKap20 til true") {
                val persongrunnlag = Persongrunnlag().apply {
                    trygdetidPerioder = mutableListOf(
                        norskTrygdetidPeriode(
                            fom = LocalDate.of(2000, 1, 1),
                            tom = LocalDate.of(2000, 3, 1) // 2 måneder
                        )
                    )
                    trygdetidPerioderKapittel20 = mutableListOf(
                        norskTrygdetidPeriode(
                            fom = LocalDate.of(2010, 1, 1),
                            tom = LocalDate.of(2015, 1, 1) // 5 år
                        )
                    )
                }
                val kravhode = Kravhode().apply {
                    regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_N_OPPTJ
                }

                val result = InngangOgEksportGrunnlagFactory.newInngangOgEksportGrunnlagForSimuleringUtland(
                    persongrunnlag, kravhode
                )

                result.treArTrygdetidNorge shouldBe false
                result.treArTrygdetidNorgeKap20 shouldBe true
            }
        }

        context("for alderspensjon 2025 (N_REG_N_OPPTJ - kun kapittel 20)") {

            test("setter kun treArTrygdetidNorgeKap20 til true når trygdetid er minst 1 år") {
                val persongrunnlag = Persongrunnlag().apply {
                    trygdetidPerioderKapittel20 = mutableListOf(
                        norskTrygdetidPeriode(
                            fom = LocalDate.of(2010, 1, 1),
                            tom = LocalDate.of(2012, 1, 1) // 2 år
                        )
                    )
                }
                val kravhode = Kravhode().apply {
                    regelverkTypeEnum = RegelverkTypeEnum.N_REG_N_OPPTJ
                }

                val result = InngangOgEksportGrunnlagFactory.newInngangOgEksportGrunnlagForSimuleringUtland(
                    persongrunnlag, kravhode
                )

                result.treArTrygdetidNorge shouldBe null // Ikke satt for 2025
                result.treArTrygdetidNorgeKap20 shouldBe true
            }

            test("setter treArTrygdetidNorgeKap20 til false når trygdetid er under 1 år") {
                val persongrunnlag = Persongrunnlag().apply {
                    trygdetidPerioderKapittel20 = mutableListOf(
                        norskTrygdetidPeriode(
                            fom = LocalDate.of(2010, 1, 1),
                            tom = LocalDate.of(2010, 6, 1) // 5 måneder
                        )
                    )
                }
                val kravhode = Kravhode().apply {
                    regelverkTypeEnum = RegelverkTypeEnum.N_REG_N_OPPTJ
                }

                val result = InngangOgEksportGrunnlagFactory.newInngangOgEksportGrunnlagForSimuleringUtland(
                    persongrunnlag, kravhode
                )

                result.treArTrygdetidNorgeKap20 shouldBe false
            }

            test("setter treArTrygdetidNorgeKap20 til false når ingen trygdetidperioder") {
                val persongrunnlag = Persongrunnlag().apply {
                    trygdetidPerioderKapittel20 = mutableListOf()
                }
                val kravhode = Kravhode().apply {
                    regelverkTypeEnum = RegelverkTypeEnum.N_REG_N_OPPTJ
                }

                val result = InngangOgEksportGrunnlagFactory.newInngangOgEksportGrunnlagForSimuleringUtland(
                    persongrunnlag, kravhode
                )

                result.treArTrygdetidNorgeKap20 shouldBe false
            }
        }

        context("filtrering av trygdetidperioder") {

            test("teller kun norske perioder (LandkodeEnum.NOR)") {
                val persongrunnlag = Persongrunnlag().apply {
                    trygdetidPerioder = mutableListOf(
                        // Norsk periode - skal telles
                        norskTrygdetidPeriode(
                            fom = LocalDate.of(2000, 1, 1),
                            tom = LocalDate.of(2000, 6, 1) // 5 måneder
                        ),
                        // Utenlandsk periode - skal ikke telles
                        TTPeriode().apply {
                            fom = LocalDate.of(2001, 1, 1).toNorwegianDateAtNoon()
                            tom = LocalDate.of(2005, 1, 1).toNorwegianDateAtNoon() // 4 år
                            landEnum = LandkodeEnum.SWE // Sverige
                        }
                    )
                }
                val kravhode = Kravhode().apply {
                    regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_OPPTJ
                }

                val result = InngangOgEksportGrunnlagFactory.newInngangOgEksportGrunnlagForSimuleringUtland(
                    persongrunnlag, kravhode
                )

                // Kun 5 måneder norsk trygdetid - under 1 år
                result.treArTrygdetidNorge shouldBe false
            }

            test("summerer flere norske perioder") {
                val persongrunnlag = Persongrunnlag().apply {
                    trygdetidPerioder = mutableListOf(
                        norskTrygdetidPeriode(
                            fom = LocalDate.of(2000, 1, 1),
                            tom = LocalDate.of(2000, 7, 3) // 184 dager
                        ),
                        norskTrygdetidPeriode(
                            fom = LocalDate.of(2005, 1, 1),
                            tom = LocalDate.of(2005, 7, 3) // 184 dager
                        )
                    )
                }
                val kravhode = Kravhode().apply {
                    regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_OPPTJ
                }

                val result = InngangOgEksportGrunnlagFactory.newInngangOgEksportGrunnlagForSimuleringUtland(
                    persongrunnlag, kravhode
                )

                // Totalt 368 dager >= 365 dager (1 år)
                result.treArTrygdetidNorge shouldBe true
            }
        }

        context("når regelverkType er null") {

            test("setter ingen trygdetid-felt") {
                val persongrunnlag = Persongrunnlag().apply {
                    trygdetidPerioder = mutableListOf(
                        norskTrygdetidPeriode(
                            fom = LocalDate.of(2000, 1, 1),
                            tom = LocalDate.of(2010, 1, 1) // 10 år
                        )
                    )
                    trygdetidPerioderKapittel20 = mutableListOf(
                        norskTrygdetidPeriode(
                            fom = LocalDate.of(2010, 1, 1),
                            tom = LocalDate.of(2020, 1, 1) // 10 år
                        )
                    )
                }
                val kravhode = Kravhode().apply {
                    regelverkTypeEnum = null
                }

                val result = InngangOgEksportGrunnlagFactory.newInngangOgEksportGrunnlagForSimuleringUtland(
                    persongrunnlag, kravhode
                )

                result.treArTrygdetidNorge shouldBe null
                result.treArTrygdetidNorgeKap20 shouldBe null
                result.eksportforbud shouldBe null
                result.fortsattMedlemFT shouldBe true
            }
        }

        context("grenseverdier for minimum trygdetid") {

            test("setter treArTrygdetidNorge til true ved nøyaktig 1 år") {
                val persongrunnlag = Persongrunnlag().apply {
                    trygdetidPerioder = mutableListOf(
                        norskTrygdetidPeriode(
                            fom = LocalDate.of(2000, 1, 1),
                            tom = LocalDate.of(2001, 1, 1) // Nøyaktig 1 år (365 dager)
                        )
                    )
                }
                val kravhode = Kravhode().apply {
                    regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_OPPTJ
                }

                val result = InngangOgEksportGrunnlagFactory.newInngangOgEksportGrunnlagForSimuleringUtland(
                    persongrunnlag, kravhode
                )

                result.treArTrygdetidNorge shouldBe true
            }

            test("setter treArTrygdetidNorge til false ved akkurat under 1 år") {
                val persongrunnlag = Persongrunnlag().apply {
                    trygdetidPerioder = mutableListOf(
                        norskTrygdetidPeriode(
                            fom = LocalDate.of(2000, 1, 1),
                            tom = LocalDate.of(2000, 12, 30) // 363 dager - tydelig under 365
                        )
                    )
                }
                val kravhode = Kravhode().apply {
                    regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_OPPTJ
                }

                val result = InngangOgEksportGrunnlagFactory.newInngangOgEksportGrunnlagForSimuleringUtland(
                    persongrunnlag, kravhode
                )

                result.treArTrygdetidNorge shouldBe false
            }
        }
    }
})

private fun norskTrygdetidPeriode(fom: LocalDate, tom: LocalDate) =
    TTPeriode().apply {
        this.fom = fom.toNorwegianDateAtNoon()
        this.tom = tom.toNorwegianDateAtNoon()
        this.landEnum = LandkodeEnum.NOR
    }
