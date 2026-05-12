package no.nav.pensjon.simulator.trygdetid

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import java.time.LocalDate

class InnlandTrygdetidUtilTest : ShouldSpec({

    context("addInnenlandsopphold when closed utenlandsperiode") {
        should("add open norsk periode") {
            val grunnlagListe: List<TrygdetidOpphold> =
                InnlandTrygdetidUtil.addInnenlandsopphold(
                    oppholdListe = listOf(
                        TrygdetidOpphold(
                            periode = TTPeriode().apply {
                                landEnum = LandkodeEnum.LTU
                                fomLd = LocalDate.of(1963, 1, 3)
                                tomLd = LocalDate.of(2027, 5, 1) // closed
                            },
                            arbeidet = false
                        )
                    ),
                    foedselsdato = LocalDate.of(1963, 1, 3)
                )

            grunnlagListe.size shouldBe 2
            with(grunnlagListe[0]) {
                periode.landEnum shouldBe LandkodeEnum.LTU
                periode.fomLd shouldBe LocalDate.of(1963, 1, 3)
                periode.tomLd shouldBe LocalDate.of(2027, 5, 1)
                arbeidet shouldBe false
            }
            with(grunnlagListe[1]) {
                periode.landEnum shouldBe LandkodeEnum.NOR
                periode.fomLd shouldBe LocalDate.of(2027, 5, 2)
                periode.tomLd shouldBe null
                arbeidet shouldBe true
            }
        }
    }

    context("addInnenlandsopphold when open utenlandsperiode after fødselsdato") {
        should("add norsk periode between fødselsdato and utenlandsperiode") {
            val grunnlagListe: List<TrygdetidOpphold> =
                InnlandTrygdetidUtil.addInnenlandsopphold(
                    oppholdListe = listOf(
                        TrygdetidOpphold(
                            periode = TTPeriode().apply {
                                landEnum = LandkodeEnum.LTU
                                fomLd = LocalDate.of(1971, 1, 1)
                                tomLd = null // open
                            },
                            arbeidet = false
                        )
                    ),
                    foedselsdato = LocalDate.of(1963, 2, 15)
                )

            grunnlagListe.size shouldBe 2
            with(grunnlagListe[0]) {
                periode.landEnum shouldBe LandkodeEnum.NOR
                periode.fomLd shouldBe LocalDate.of(1963, 2, 15)
                periode.tomLd shouldBe LocalDate.of(1970, 12, 31)
                arbeidet shouldBe true
            }
            with(grunnlagListe[1]) {
                periode.landEnum shouldBe LandkodeEnum.LTU
                periode.fomLd shouldBe LocalDate.of(1971, 1, 1)
                periode.tomLd shouldBe null
                arbeidet shouldBe false
            }
        }
    }

    context("addInnenlandsopphold") {
        should("handle unsorted open perioder") {
            val grunnlagListe: List<TrygdetidOpphold> =
                InnlandTrygdetidUtil.addInnenlandsopphold(
                    oppholdListe = listOf(
                        TrygdetidOpphold(
                            periode = TTPeriode().apply {
                                landEnum = LandkodeEnum.FRA
                                fomLd = LocalDate.of(2024, 9, 4)
                                tomLd = null // open
                            },
                            arbeidet = false
                        ),
                        TrygdetidOpphold(
                            periode = TTPeriode().apply {
                                landEnum = LandkodeEnum.FRA
                                fomLd = LocalDate.of(2024, 6, 4) // before item above => unsorted
                                tomLd = null // open
                            },
                            arbeidet = true
                        )
                    ),
                    foedselsdato = LocalDate.of(1963, 2, 15)
                )

            grunnlagListe.size shouldBe 3
            with(grunnlagListe[0]) {
                periode.landEnum shouldBe LandkodeEnum.NOR
                periode.fomLd shouldBe LocalDate.of(1963, 2, 15)
                periode.tomLd shouldBe LocalDate.of(2024, 6, 3)
                arbeidet shouldBe true
            }
            with(grunnlagListe[1]) {
                periode.landEnum shouldBe LandkodeEnum.FRA
                periode.fomLd shouldBe LocalDate.of(2024, 6, 4)
                periode.tomLd shouldBe null
                arbeidet shouldBe true
            }
            with(grunnlagListe[2]) {
                periode.landEnum shouldBe LandkodeEnum.FRA
                periode.fomLd shouldBe LocalDate.of(2024, 9, 4)
                periode.tomLd shouldBe null
                arbeidet shouldBe false
            }
        }

        should("insert missing perioder") {
            val grunnlagListe: List<TrygdetidOpphold> =
                InnlandTrygdetidUtil.addInnenlandsopphold(
                    oppholdListe = listOf(
                        TrygdetidOpphold(
                            periode = TTPeriode().apply {
                                landEnum = LandkodeEnum.FRA
                                fomLd = LocalDate.of(2024, 6, 4)
                                tomLd = LocalDate.of(2024, 6, 30)
                            },
                            arbeidet = true
                        ),
                        // missing periode 2024-07-01 to 2024-09-03
                        TrygdetidOpphold(
                            periode = TTPeriode().apply {
                                landEnum = LandkodeEnum.FRA
                                fomLd = LocalDate.of(2024, 9, 4)
                                tomLd = null
                            },
                            arbeidet = false
                        )
                    ),
                    foedselsdato = LocalDate.of(1963, 2, 15)
                )

            grunnlagListe.size shouldBe 4
            with(grunnlagListe[0]) {
                periode.landEnum shouldBe LandkodeEnum.NOR
                periode.fomLd shouldBe LocalDate.of(1963, 2, 15)
                periode.tomLd shouldBe LocalDate.of(2024, 6, 3)
                arbeidet shouldBe true
            }
            with(grunnlagListe[1]) {
                periode.landEnum shouldBe LandkodeEnum.FRA
                periode.fomLd shouldBe LocalDate.of(2024, 6, 4)
                periode.tomLd shouldBe LocalDate.of(2024, 6, 30)
                arbeidet shouldBe true
            }
            with(grunnlagListe[2]) {
                periode.landEnum shouldBe LandkodeEnum.NOR
                periode.fomLd shouldBe LocalDate.of(2024, 7, 1)
                periode.tomLd shouldBe LocalDate.of(2024, 9, 3)
                arbeidet shouldBe true
            }
            with(grunnlagListe[3]) {
                periode.landEnum shouldBe LandkodeEnum.FRA
                periode.fomLd shouldBe LocalDate.of(2024, 9, 4)
                periode.tomLd shouldBe null
                arbeidet shouldBe false
            }
        }

        should("handle 1-dags utenlandsopphold") {
            val grunnlagListe: List<TrygdetidOpphold> =
                InnlandTrygdetidUtil.addInnenlandsopphold(
                    oppholdListe = listOf(
                        TrygdetidOpphold(
                            periode = TTPeriode().apply {
                                landEnum = LandkodeEnum.DZA
                                fomLd = LocalDate.of(2026, 4, 27)
                                tomLd = LocalDate.of(2026, 4, 30)
                            },
                            arbeidet = true
                        ),
                        TrygdetidOpphold(
                            periode = TTPeriode().apply {
                                landEnum = LandkodeEnum.AND
                                fomLd = LocalDate.of(2026, 4, 30)
                                tomLd = LocalDate.of(2026, 4, 30)
                            },
                            arbeidet = false
                        )
                    ),
                    foedselsdato = LocalDate.of(1963, 2, 15)
                )

            grunnlagListe.size shouldBe 4
            with(grunnlagListe[0]) {
                periode.landEnum shouldBe LandkodeEnum.NOR
                periode.fomLd shouldBe LocalDate.of(1963, 2, 15)
                periode.tomLd shouldBe LocalDate.of(2026, 4, 26)
                arbeidet shouldBe true
            }
            with(grunnlagListe[1]) {
                periode.landEnum shouldBe LandkodeEnum.DZA
                periode.fomLd shouldBe LocalDate.of(2026, 4, 27)
                periode.tomLd shouldBe LocalDate.of(2026, 4, 30)
                arbeidet shouldBe true
            }
            with(grunnlagListe[2]) {
                periode.landEnum shouldBe LandkodeEnum.AND
                periode.fomLd shouldBe LocalDate.of(2026, 4, 30)
                periode.tomLd shouldBe LocalDate.of(2026, 4, 30)
                arbeidet shouldBe false
            }
            with(grunnlagListe[3]) {
                periode.landEnum shouldBe LandkodeEnum.NOR
                periode.fomLd shouldBe LocalDate.of(2026, 5, 1)
                periode.tomLd shouldBe null
                arbeidet shouldBe true
            }
        }
    }
})
