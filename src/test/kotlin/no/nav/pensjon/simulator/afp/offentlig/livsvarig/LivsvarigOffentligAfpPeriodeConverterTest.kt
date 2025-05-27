package no.nav.pensjon.simulator.afp.offentlig.livsvarig

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class LivsvarigOffentligAfpPeriodeConverterTest : FunSpec({

    test("aarligePerioder med 2 ytelser") {
        LivsvarigOffentligAfpPeriodeConverter.aarligePerioder(
            result = LivsvarigOffentligAfpResult(
                pid = pid.value,
                afpYtelseListe = listOf(
                    LivsvarigOffentligAfpYtelseMedDelingstall(
                        pensjonBeholdning = 123000,
                        afpYtelsePerAar = 144.12,
                        delingstall = 1.23,
                        gjelderFom = LocalDate.of(2021, 3, 1), // '3' påvirker maanederMedUttakSats
                        gjelderFomAlder = Alder(63, 0)
                    ),
                    LivsvarigOffentligAfpYtelseMedDelingstall(
                        pensjonBeholdning = 234000,
                        afpYtelsePerAar = 24.24,
                        delingstall = 2.34,
                        gjelderFom = LocalDate.of(2022, 5, 1),
                        gjelderFomAlder = Alder(64, 2)
                    )
                )
            ),
            foedselMaaned = 11
        ) shouldBe listOf(
            // maanederMedUttakSats = 12 - 3 + 1 = 10
            LivsvarigOffentligAfpOutput(
                alderAar = 63,
                beloep = 142, // = 144.12 / 12 * 10 + 24.24 / 12 * 11
                maanedligBeloep = 12
            ),
            LivsvarigOffentligAfpOutput(
                alderAar = 64,
                beloep = 24,
                maanedligBeloep = 2
            )
        )
    }

    test("aarligePerioder med 1 ytelse, mer enn 0 aldersmåneder") {
        LivsvarigOffentligAfpPeriodeConverter.aarligePerioder(
            result = LivsvarigOffentligAfpResult(
                pid = pid.value,
                afpYtelseListe = listOf(
                    LivsvarigOffentligAfpYtelseMedDelingstall(
                        pensjonBeholdning = 123000,
                        afpYtelsePerAar = 144.144,
                        delingstall = 2.34,
                        gjelderFom = LocalDate.of(2023, 2, 1), // '2' påvirker maanederMedUttakSats
                        gjelderFomAlder = Alder(63, 4) // mer enn 0 aldersmåneder
                    )
                )
            ),
            foedselMaaned = 5 // => uttakMaanedVedUttaksalderMedHeleAar = 6 => maanederMedUttakSats = 6 - 2 = 4
        ) shouldBe listOf(
            LivsvarigOffentligAfpOutput(
                alderAar = 63,
                beloep = 48, // = 144.144 / 12 * 4
                maanedligBeloep = 12
            ),
            LivsvarigOffentligAfpOutput(
                alderAar = 64,
                beloep = 144,
                maanedligBeloep = 12
            )
        )
    }

    test("aarligePerioder med 1 ytelse, 0 aldersmåneder") {
        LivsvarigOffentligAfpPeriodeConverter.aarligePerioder(
            result = LivsvarigOffentligAfpResult(
                pid = pid.value,
                afpYtelseListe = listOf(
                    LivsvarigOffentligAfpYtelseMedDelingstall(
                        pensjonBeholdning = 123000,
                        afpYtelsePerAar = 144.144,
                        delingstall = 2.34,
                        gjelderFom = LocalDate.of(2021, 1, 1),
                        gjelderFomAlder = Alder(63, 0) // 0 aldersmåneder
                    )
                )
            ),
            foedselMaaned = 12
        ) shouldBe listOf(
            LivsvarigOffentligAfpOutput(
                alderAar = 63,
                beloep = 144,
                maanedligBeloep = 12
            )
        )
    }
})
