package no.nav.pensjon.simulator.trygdetid

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.trygd.TrygdetidOpphold
import no.nav.pensjon.simulator.testutil.TestDateUtil.atNoon
import java.time.LocalDate

class InnlandTrygdetidUtilTest : FunSpec({

    test("addInnenlandsopphold when closed utenlandsperiode: should add open norsk periode") {
        val grunnlagListe: List<TrygdetidOpphold> =
            InnlandTrygdetidUtil.addInnenlandsopphold(
                oppholdListe = listOf(
                    TrygdetidOpphold(
                        periode = TTPeriode().apply {
                            landEnum = LandkodeEnum.LTU
                            fom = atNoon(1963, 1, 3)
                            tom = atNoon(2027, 5, 1) // closed
                        },
                        arbeidet = false
                    )
                ),
                foedselsdato = LocalDate.of(1963, 1, 3)
            )

        grunnlagListe.size shouldBe 2
        with(grunnlagListe[0]) {
            periode.landEnum shouldBe LandkodeEnum.LTU
            periode.fom shouldBe atNoon(1963, 1, 3)
            periode.tom shouldBe atNoon(2027, 5, 1)
            arbeidet shouldBe false
        }
        with(grunnlagListe[1]) {
            periode.landEnum shouldBe LandkodeEnum.NOR
            periode.fom shouldBe atNoon(2027, 5, 2)
            periode.tom shouldBe null
            arbeidet shouldBe true
        }
    }

    test("addInnenlandsopphold when open utenlandsperiode after fødselsdato: should add norsk periode between fødselsdato and utenlandsperiode") {
        val grunnlagListe: List<TrygdetidOpphold> =
            InnlandTrygdetidUtil.addInnenlandsopphold(
                oppholdListe = listOf(
                    TrygdetidOpphold(
                        periode = TTPeriode().apply {
                            landEnum = LandkodeEnum.LTU
                            fom = atNoon(1971, 1, 1)
                            tom = null // open
                        },
                        arbeidet = false
                    )
                ),
                foedselsdato = LocalDate.of(1963, 2, 15)
            )

        grunnlagListe.size shouldBe 2
        with(grunnlagListe[0]) {
            periode.landEnum shouldBe LandkodeEnum.NOR
            periode.fom shouldBe atNoon(1963, 2, 15)
            periode.tom shouldBe atNoon(1970, 12, 31)
            arbeidet shouldBe true
        }
        with(grunnlagListe[1]) {
            periode.landEnum shouldBe LandkodeEnum.LTU
            periode.fom shouldBe atNoon(1971, 1, 1)
            periode.tom shouldBe null
            arbeidet shouldBe false
        }
    }

    test("addInnenlandsopphold handles unsorted open perioder") {
        val grunnlagListe: List<TrygdetidOpphold> =
            InnlandTrygdetidUtil.addInnenlandsopphold(
                oppholdListe = listOf(
                    TrygdetidOpphold(
                        periode = TTPeriode().apply {
                            landEnum = LandkodeEnum.FRA
                            fom = atNoon(2024, 9, 4)
                            tom = null // open
                        },
                        arbeidet = false
                    ),
                    TrygdetidOpphold(
                        periode = TTPeriode().apply {
                            landEnum = LandkodeEnum.FRA
                            fom = atNoon(2024, 6, 4) // before item above => unsorted
                            tom = null // open
                        },
                        arbeidet = true
                    )
                ),
                foedselsdato = LocalDate.of(1963, 2, 15)
            )

        grunnlagListe.size shouldBe 3
        with(grunnlagListe[0]) {
            periode.landEnum shouldBe LandkodeEnum.NOR
            periode.fom shouldBe atNoon(1963, 2, 15)
            periode.tom shouldBe atNoon(2024, 6, 3)
            arbeidet shouldBe true
        }
        with(grunnlagListe[1]) {
            periode.landEnum shouldBe LandkodeEnum.FRA
            periode.fom shouldBe atNoon(2024, 6, 4)
            periode.tom shouldBe null
            arbeidet shouldBe true
        }
        with(grunnlagListe[2]) {
            periode.landEnum shouldBe LandkodeEnum.FRA
            periode.fom shouldBe atNoon(2024, 9, 4)
            periode.tom shouldBe null
            arbeidet shouldBe false
        }
    }

    test("addInnenlandsopphold inserts missing perioder") {
        val grunnlagListe: List<TrygdetidOpphold> =
            InnlandTrygdetidUtil.addInnenlandsopphold(
                oppholdListe = listOf(
                    TrygdetidOpphold(
                        periode = TTPeriode().apply {
                            landEnum = LandkodeEnum.FRA
                            fom = atNoon(2024, 6, 4)
                            tom = atNoon(2024, 6, 30)
                        },
                        arbeidet = true
                    ),
                    // missing periode 2024-07-01 to 2024-09-03
                    TrygdetidOpphold(
                        periode = TTPeriode().apply {
                            landEnum = LandkodeEnum.FRA
                            fom = atNoon(2024, 9, 4)
                            tom = null
                        },
                        arbeidet = false
                    )
                ),
                foedselsdato = LocalDate.of(1963, 2, 15)
            )

        grunnlagListe.size shouldBe 4
        with(grunnlagListe[0]) {
            periode.landEnum shouldBe LandkodeEnum.NOR
            periode.fom shouldBe atNoon(1963, 2, 15)
            periode.tom shouldBe atNoon(2024, 6, 3)
            arbeidet shouldBe true
        }
        with(grunnlagListe[1]) {
            periode.landEnum shouldBe LandkodeEnum.FRA
            periode.fom shouldBe atNoon(2024, 6, 4)
            periode.tom shouldBe atNoon(2024, 6, 30)
            arbeidet shouldBe true
        }
        with(grunnlagListe[2]) {
            periode.landEnum shouldBe LandkodeEnum.NOR
            periode.fom shouldBe atNoon(2024, 7, 1)
            periode.tom shouldBe atNoon(2024, 9, 3)
            arbeidet shouldBe true
        }
        with(grunnlagListe[3]) {
            periode.landEnum shouldBe LandkodeEnum.FRA
            periode.fom shouldBe atNoon(2024, 9, 4)
            periode.tom shouldBe null
            arbeidet shouldBe false
        }
    }
})
