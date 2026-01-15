package no.nav.pensjon.simulator.trygdetid

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import java.time.LocalDate
import java.util.*
import java.util.Calendar.DECEMBER
import java.util.Calendar.FEBRUARY
import java.util.Calendar.JANUARY
import java.util.Calendar.MARCH
import java.util.Calendar.MAY

class UtlandPeriodeTrygdetidMapperTest : FunSpec({

    test("utlandTrygdetidGrunnlag removes overlap") {
        val result = UtlandPeriodeTrygdetidMapper.utlandTrygdetidsgrunnlag(
            mutableListOf(
                UtlandPeriode(
                    fom = LocalDate.of(2024, 1, 1),
                    tom = LocalDate.of(2024, 2, 1),
                    land = LandkodeEnum.DNK,
                    arbeidet = false
                ), UtlandPeriode(
                    fom = LocalDate.of(2024, 2, 1), // overlap
                    tom = LocalDate.of(2024, 3, 31),
                    land = LandkodeEnum.SWE,
                    arbeidet = false
                )
            )
        )

        result.size shouldBe 2
        with(result[0].periode) {
            fom shouldBe dateAtNoon(2024, JANUARY, 1)
            tom shouldBe dateAtNoon(2024, JANUARY, 31) // overlap removed
            landEnum shouldBe LandkodeEnum.DNK
        }
        with(result[1].periode) {
            fom shouldBe dateAtNoon(2024, FEBRUARY, 1)
            tom shouldBe dateAtNoon(2024, MARCH, 31)
            landEnum shouldBe LandkodeEnum.SWE
        }
    }

    test("utlandTrygdetidGrunnlag sorts list") {
        val result = UtlandPeriodeTrygdetidMapper.utlandTrygdetidsgrunnlag(
            mutableListOf(
                UtlandPeriode(
                    fom = LocalDate.of(2024, 2, 1),
                    tom = LocalDate.of(2024, 3, 31),
                    land = LandkodeEnum.SWE,
                    arbeidet = false
                ),
                UtlandPeriode(
                    fom = LocalDate.of(2024, 1, 1),
                    tom = LocalDate.of(2024, 1, 31),
                    land = LandkodeEnum.DNK,
                    arbeidet = false
                )
            )
        )

        result.size shouldBe 2
        with(result[0].periode) {
            fom shouldBe dateAtNoon(2024, JANUARY, 1)
            tom shouldBe dateAtNoon(2024, JANUARY, 31)
            landEnum shouldBe LandkodeEnum.DNK
        }
        with(result[1].periode) {
            fom shouldBe dateAtNoon(2024, FEBRUARY, 1)
            tom shouldBe dateAtNoon(2024, MARCH, 31)
            landEnum shouldBe LandkodeEnum.SWE
        }
    }

    test("utlandTrygdetidGrunnlag handles list with 1 item") {
        val result = UtlandPeriodeTrygdetidMapper.utlandTrygdetidsgrunnlag(
            mutableListOf(
                UtlandPeriode(
                    fom = LocalDate.of(2024, 1, 1),
                    tom = LocalDate.of(2024, 2, 1),
                    land = LandkodeEnum.DNK,
                    arbeidet = true
                )
            )
        )

        result.size shouldBe 1
        with(result[0].periode) {
            fom shouldBe dateAtNoon(2024, JANUARY, 1)
            tom shouldBe dateAtNoon(2024, FEBRUARY, 1)
            landEnum shouldBe LandkodeEnum.DNK
        }
    }

    test("utlandTrygdetidGrunnlag when closed utenlandsperiode") {
        val result: List<TrygdetidOpphold> =
            UtlandPeriodeTrygdetidMapper.utlandTrygdetidsgrunnlag(
                utlandPeriodeListe = mutableListOf(
                    UtlandPeriode(
                        fom = LocalDate.of(1971, 1, 1),
                        tom = LocalDate.of(2027, 5, 1), // closed
                        land = LandkodeEnum.LTU,
                        arbeidet = false
                    )
                ),
                trygdetidsgrunnlagMedPensjonspoengListe = norskTrygdetid(1981, 1980, 1986, 1983, 1987, 1982)
            )

        result.size shouldBe 9
        assertOpphold(actual = result[0], fomAar = 1971, tomAar = 1979, land = LandkodeEnum.LTU, arbeidet = false)
        assertOpphold(actual = result[1], fomAar = 1980, tomAar = 1980, land = LandkodeEnum.NOR, arbeidet = true)
        assertOpphold(actual = result[2], fomAar = 1981, tomAar = 1981, land = LandkodeEnum.NOR, arbeidet = true)
        assertOpphold(actual = result[3], fomAar = 1982, tomAar = 1982, land = LandkodeEnum.NOR, arbeidet = true)
        assertOpphold(actual = result[4], fomAar = 1983, tomAar = 1983, land = LandkodeEnum.NOR, arbeidet = true)
        assertOpphold(actual = result[5], fomAar = 1984, tomAar = 1985, land = LandkodeEnum.LTU, arbeidet = false)
        assertOpphold(actual = result[6], fomAar = 1986, tomAar = 1986, land = LandkodeEnum.NOR, arbeidet = true)
        assertOpphold(actual = result[7], fomAar = 1987, tomAar = 1987, land = LandkodeEnum.NOR, arbeidet = true)
        assertOpphold(actual = result[8], fomAar = 1988, tomDato = dateAtNoon(2027, MAY, 1), land = LandkodeEnum.LTU, arbeidet = false)
    }

    test("utlandTrygdetidGrunnlag when open utenlandsperiode") {
        val result: List<TrygdetidOpphold> =
            UtlandPeriodeTrygdetidMapper.utlandTrygdetidsgrunnlag(
                utlandPeriodeListe = mutableListOf(
                    UtlandPeriode(
                        fom = LocalDate.of(1971, 1, 1),
                        tom = null, // open
                        land = LandkodeEnum.FIN,
                        arbeidet = true
                    )
                ),
                trygdetidsgrunnlagMedPensjonspoengListe = norskTrygdetid(1981, 1980, 1986, 1983, 1984, 1982)
            )

        result.size shouldBe 9
        assertOpphold(actual = result[0], fomAar = 1971, tomAar = 1979, land = LandkodeEnum.FIN, arbeidet = true)
        assertOpphold(actual = result[1], fomAar = 1980, tomAar = 1980, land = LandkodeEnum.NOR, arbeidet = true)
        assertOpphold(actual = result[2], fomAar = 1981, tomAar = 1981, land = LandkodeEnum.NOR, arbeidet = true)
        assertOpphold(actual = result[3], fomAar = 1982, tomAar = 1982, land = LandkodeEnum.NOR, arbeidet = true)
        assertOpphold(actual = result[4], fomAar = 1983, tomAar = 1983, land = LandkodeEnum.NOR, arbeidet = true)
        assertOpphold(actual = result[5], fomAar = 1984, tomAar = 1984, land = LandkodeEnum.NOR, arbeidet = true)
        assertOpphold(actual = result[6], fomAar = 1985, tomAar = 1985, land = LandkodeEnum.FIN, arbeidet = true)
        assertOpphold(actual = result[7], fomAar = 1986, tomAar = 1986, land = LandkodeEnum.NOR, arbeidet = true)
        assertOpphold(actual = result[8], fomAar = 1987, tomAar = null, land = LandkodeEnum.FIN, arbeidet = true)
    }

    test("utlandTrygdetidGrunnlag handles empty list") {
        UtlandPeriodeTrygdetidMapper.utlandTrygdetidsgrunnlag(mutableListOf()).size shouldBe 0
    }
})

private fun norskTrygdetid(vararg aarListe: Int): List<TrygdetidOpphold> =
    aarListe.map {
        TrygdetidOpphold(
            periode = TTPeriode().apply {
                fom = dateAtNoon(it, JANUARY, 1)
                tom = dateAtNoon(it, DECEMBER, 31)
                landEnum = LandkodeEnum.NOR
            },
            arbeidet = true
        )
    }

private fun assertOpphold(
    actual: TrygdetidOpphold,
    fomAar: Int,
    tomAar: Int? = null,
    tomDato: Date? = null,
    land: LandkodeEnum,
    arbeidet: Boolean
) {
    with(actual) {
        this.arbeidet shouldBe arbeidet
        with(periode) {
            landEnum shouldBe land
            fom shouldBe dateAtNoon(fomAar, JANUARY, 1)
            tom shouldBe (tomDato ?: tomAar?.let { dateAtNoon(it, DECEMBER, 31) })
        }
    }
}
