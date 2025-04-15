package no.nav.pensjon.simulator.core.afp.offentlig.livsvarig

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpOffentligLivsvarig
import java.time.LocalDate

class LivsvarigOffentligAfpUtilTest : FunSpec({

    test("getLivsvarigOffentligAfp should give null when 'gjelder fom' is after knekkpunktdato") {
        LivsvarigOffentligAfpUtil.getLivsvarigOffentligAfp(
            resultatListe = listOf(
                ytelse(afpYtelsePerAar = 12.3, gjelderFom = LocalDate.of(2025, 3, 1))
            ),
            knekkpunktDato = LocalDate.of(2025, 2, 1)
        ) shouldBe null
    }

    test("getLivsvarigOffentligAfp should give result when 'gjelder fom' is before knekkpunktdato") {
        val result = LivsvarigOffentligAfpUtil.getLivsvarigOffentligAfp(
            resultatListe = listOf(
                ytelse(afpYtelsePerAar = 12.3, gjelderFom = LocalDate.of(2025, 1, 1))
            ),
            knekkpunktDato = LocalDate.of(2025, 2, 1)
        )

        result?.let {
            it shouldBeEqualToComparingFields AfpOffentligLivsvarig().apply {
                bruttoPerAr = 12.3
                uttaksdato = LocalDate.of(2025, 1, 1)
            }
        }
    }

    test("getLivsvarigOffentligAfp should pick max. 'gjelder fom' before or on knekkpunktdato") {
        val result = LivsvarigOffentligAfpUtil.getLivsvarigOffentligAfp(
            resultatListe = listOf(
                ytelse(afpYtelsePerAar = 12.3, gjelderFom = LocalDate.of(2025, 1, 1)), // before
                ytelse(afpYtelsePerAar = 23.4, gjelderFom = LocalDate.of(2025, 2, 1)), // on => max gjelderFom
                ytelse(afpYtelsePerAar = 34.5, gjelderFom = LocalDate.of(2025, 3, 1))  // after
            ),
            knekkpunktDato = LocalDate.of(2025, 2, 1)
        )

        result?.let {
            it shouldBeEqualToComparingFields AfpOffentligLivsvarig().apply {
                bruttoPerAr = 23.4
                uttaksdato = LocalDate.of(2025, 2, 1)
            }
        }
    }
})

private fun ytelse(afpYtelsePerAar: Double, gjelderFom: LocalDate) =
    LivsvarigOffentligAfpYtelseMedDelingstall(
        pensjonBeholdning = 1234,
        afpYtelsePerAar,
        delingstall = 1.2,
        gjelderFom,
        gjelderFomAlder = Alder(63, 0)
    )
