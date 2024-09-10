package no.nav.pensjon.simulator.core.trygd

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.kode.LandCti
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.fromLocalDate
import no.nav.pensjon.simulator.core.trygd.DateUtil.atNoon
import no.nav.pensjon.simulator.core.util.DateNoonExtension.noon
import java.time.LocalDate

class InnlandTrygdetidGrunnlagInserterTest : FunSpec({

    test("createTrygdeTidGrunnlagForInnlandPerioder handles unsorted endless perioder") {
        val grunnlagListe: List<TrygdetidOpphold> =
            InnlandTrygdetidGrunnlagInserter.createTrygdetidGrunnlagForInnlandPerioder(
                trygdetidGrunnlagListe = listOf(
                    TrygdetidOpphold(
                        periode = TTPeriode(
                            land = LandCti("FRA"),
                            fom = atNoon(2024, 9, 4),
                            tom = null // endless
                        ),
                        arbeidet = false
                    ),
                    TrygdetidOpphold(
                        periode = TTPeriode(
                            land = LandCti("FRA"),
                            fom = atNoon(2024, 6, 4), // before item above => unsorted
                            tom = null // endless
                        ),
                        arbeidet = true
                    )
                ),
                foedselDato = LocalDate.of(1963, 2, 15)
            )

        grunnlagListe.size shouldBe 3
        with(grunnlagListe[0]) {
            periode.land?.kode shouldBe "NOR"
            periode.fom shouldBe atNoon(1963, 2, 15)
            periode.tom shouldBe atNoon(2024, 6, 3)
            arbeidet shouldBe true
        }
        with(grunnlagListe[1]) {
            periode.land?.kode shouldBe "FRA"
            periode.fom shouldBe atNoon(2024, 6, 4)
            periode.tom shouldBe null
            arbeidet shouldBe true
        }
        with(grunnlagListe[2]) {
            periode.land?.kode shouldBe "FRA"
            periode.fom shouldBe atNoon(2024, 9, 4)
            periode.tom shouldBe null
            arbeidet shouldBe false
        }
    }

    test("createTrygdeTidGrunnlagForInnlandPerioder inserts missing perioder") {
        val grunnlagListe: List<TrygdetidOpphold> =
            InnlandTrygdetidGrunnlagInserter.createTrygdetidGrunnlagForInnlandPerioder(
                trygdetidGrunnlagListe = listOf(
                    TrygdetidOpphold(
                        periode = TTPeriode(
                            land = LandCti("FRA"),
                            fom = atNoon(2024, 6, 4),
                            tom = atNoon(2024, 6, 30)
                        ),
                        arbeidet = true
                    ),
                    // missing periode 2024-07-01 to 2024-09-03
                    TrygdetidOpphold(
                        periode = TTPeriode(
                            land = LandCti("FRA"),
                            fom = atNoon(2024, 9, 4),
                            tom = null
                        ),
                        arbeidet = false
                    )
                ),
                foedselDato = LocalDate.of(1963, 2, 15)
            )

        grunnlagListe.size shouldBe 4
        with(grunnlagListe[0]) {
            periode.land?.kode shouldBe "NOR"
            periode.fom shouldBe atNoon(1963, 2, 15)
            periode.tom shouldBe atNoon(2024, 6, 3)
            arbeidet shouldBe true
        }
        with(grunnlagListe[1]) {
            periode.land?.kode shouldBe "FRA"
            periode.fom shouldBe atNoon(2024, 6, 4)
            periode.tom shouldBe atNoon(2024, 6, 30)
            arbeidet shouldBe true
        }
        with(grunnlagListe[2]) {
            periode.land?.kode shouldBe "NOR"
            periode.fom shouldBe atNoon(2024, 7, 1)
            periode.tom shouldBe atNoon(2024, 9, 3)
            arbeidet shouldBe true
        }
        with(grunnlagListe[3]) {
            periode.land?.kode shouldBe "FRA"
            periode.fom shouldBe atNoon(2024, 9, 4)
            periode.tom shouldBe null
            arbeidet shouldBe false
        }
    }
})

object DateUtil {
    fun atNoon(year: Int, month: Int, dayOfMonth: Int) =
        fromLocalDate(LocalDate.of(year, month, dayOfMonth))?.noon()
}
