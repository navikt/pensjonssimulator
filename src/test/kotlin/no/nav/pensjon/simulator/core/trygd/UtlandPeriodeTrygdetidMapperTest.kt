package no.nav.pensjon.simulator.core.trygd

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.testutil.TestDateUtil
import java.time.LocalDate
import java.util.Calendar

class UtlandPeriodeTrygdetidMapperTest : FunSpec({

    test("utlandTrygdetidGrunnlag removes overlap") {
        val result = UtlandPeriodeTrygdetidMapper.utlandTrygdetidGrunnlag(
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
            fom shouldBe TestDateUtil.dateAtNoon(2024, Calendar.JANUARY, 1)
            tom shouldBe TestDateUtil.dateAtNoon(2024, Calendar.JANUARY, 31) // overlap removed
            land shouldBe LandkodeEnum.DNK
        }
        with(result[1].periode) {
            fom shouldBe TestDateUtil.dateAtNoon(2024, Calendar.FEBRUARY, 1)
            tom shouldBe TestDateUtil.dateAtNoon(2024, Calendar.MARCH, 31)
            land shouldBe LandkodeEnum.SWE
        }
    }

    test("utlandTrygdetidGrunnlag sorts list") {
        val result = UtlandPeriodeTrygdetidMapper.utlandTrygdetidGrunnlag(
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
            fom shouldBe TestDateUtil.dateAtNoon(2024, Calendar.JANUARY, 1)
            tom shouldBe TestDateUtil.dateAtNoon(2024, Calendar.JANUARY, 31)
            land shouldBe LandkodeEnum.DNK
        }
        with(result[1].periode) {
            fom shouldBe TestDateUtil.dateAtNoon(2024, Calendar.FEBRUARY, 1)
            tom shouldBe TestDateUtil.dateAtNoon(2024, Calendar.MARCH, 31)
            land shouldBe LandkodeEnum.SWE
        }
    }

    test("utlandTrygdetidGrunnlag handles list with 1 item") {
        val result = UtlandPeriodeTrygdetidMapper.utlandTrygdetidGrunnlag(
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
            fom shouldBe TestDateUtil.dateAtNoon(2024, Calendar.JANUARY, 1)
            tom shouldBe TestDateUtil.dateAtNoon(2024, Calendar.FEBRUARY, 1)
            land shouldBe LandkodeEnum.DNK
        }
    }


    test("utlandTrygdetidGrunnlag handles empty list") {
        UtlandPeriodeTrygdetidMapper.utlandTrygdetidGrunnlag(mutableListOf()).size shouldBe 0
    }
})
