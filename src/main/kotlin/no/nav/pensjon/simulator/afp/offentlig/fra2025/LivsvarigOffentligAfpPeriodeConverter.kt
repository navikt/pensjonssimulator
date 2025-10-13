package no.nav.pensjon.simulator.afp.offentlig.fra2025

import no.nav.pensjon.simulator.afp.offentlig.fra2025.grunnlag.LivsvarigOffentligAfpResult
import no.nav.pensjon.simulator.afp.offentlig.fra2025.grunnlag.LivsvarigOffentligAfpYtelseMedDelingstall
import no.nav.pensjon.simulator.tech.time.DateUtil.MAANEDER_PER_AAR
import kotlin.math.roundToInt

object LivsvarigOffentligAfpPeriodeConverter {

    private const val MIN_ANTALL_MAANEDER_FRA_IVERKSETTELSE_TIL_UTTAK = 1

    fun aarligePerioder(
        result: LivsvarigOffentligAfpResult?,
        foedselMaaned: Int?
    ): List<LivsvarigOffentligAfpOutput> {
        if (result == null || result.afpYtelseListe.isEmpty() || foedselMaaned == null) {
            return emptyList()
        }

        val ytelseListe = result.afpYtelseListe
        val ytelseVedUttak = ytelseListe[0]
        val uttakMaaned = ytelseVedUttak.gjelderFom.monthValue

        return when {
            ytelseListe.size > 1 -> {
                opprettPerioder(
                    ytelseVedUttak = ytelseListe[0],
                    maanederMedUttakSats = MAANEDER_PER_AAR - uttakMaaned + 1, // uttakMaaned skal inkluderes i antall måneder med uttakssats
                    ytelseVedAndreAaret = ytelseListe[1],
                    maanederMedOppdatertUttakSats = foedselMaaned
                )
            }

            ytelseVedUttak.gjelderFomAlder.maaneder > 0 -> {
                opprettPerioder(
                    ytelseVedUttak = ytelseListe[0],
                    maanederMedUttakSats = maanederFraFoersteUttakTilFyltAar(uttakMaaned, foedselMaaned),
                    ytelseVedAndreAaret = ytelseListe[0],
                    maanederMedOppdatertUttakSats = 0
                )
            }

            else -> listOf(
                LivsvarigOffentligAfpOutput(
                    alderAar = ytelseVedUttak.gjelderFomAlder.aar,
                    beloep = ytelseVedUttak.afpYtelsePerAar.toInt(),
                    maanedligBeloep = (ytelseVedUttak.afpYtelsePerAar / MAANEDER_PER_AAR).roundToInt()
                )
            )
        }
    }

    private fun maanederFraFoersteUttakTilFyltAar(uttakMaaned: Int, foedselMaaned: Int): Int {
        val uttakMaanedVedUttaksalderMedHeleAar =
            foedselMaaned + MIN_ANTALL_MAANEDER_FRA_IVERKSETTELSE_TIL_UTTAK

        return when {
            uttakMaaned == uttakMaanedVedUttaksalderMedHeleAar -> MAANEDER_PER_AAR
            uttakMaaned > uttakMaanedVedUttaksalderMedHeleAar -> MAANEDER_PER_AAR - uttakMaaned + 1 + foedselMaaned  // uttakMaaned skal inkluderes i antall måneder med uttakssats. Periode: [uttakMaaned...aarskifte] + [aarskifte...foedselssmaaned]
            else -> uttakMaanedVedUttaksalderMedHeleAar - uttakMaaned // periode: [uttakMaaned...uttakMaanedVedUttaksalderMedHeleAr]
        }
    }

    private fun opprettPerioder(
        ytelseVedUttak: LivsvarigOffentligAfpYtelseMedDelingstall,
        maanederMedUttakSats: Int,
        ytelseVedAndreAaret: LivsvarigOffentligAfpYtelseMedDelingstall,
        maanederMedOppdatertUttakSats: Int,
    ): List<LivsvarigOffentligAfpOutput> {
        val alderAarVedUttak = ytelseVedUttak.gjelderFomAlder.aar
        val satsPerMaanedVedUttak = ytelseVedUttak.afpYtelsePerAar / MAANEDER_PER_AAR
        val ytelsePerAarEtterOppdatering = ytelseVedAndreAaret.afpYtelsePerAar
        val satsPerMaanedEtterOppdatering = ytelsePerAarEtterOppdatering / MAANEDER_PER_AAR

        return listOf(
            LivsvarigOffentligAfpOutput(
                alderAar = alderAarVedUttak,
                beloep = (satsPerMaanedVedUttak * maanederMedUttakSats + satsPerMaanedEtterOppdatering * maanederMedOppdatertUttakSats).roundToInt(),
                maanedligBeloep = satsPerMaanedVedUttak.roundToInt()
            ),
            LivsvarigOffentligAfpOutput(
                alderAar = alderAarVedUttak + 1,
                beloep = ytelsePerAarEtterOppdatering.roundToInt(),
                maanedligBeloep = satsPerMaanedEtterOppdatering.roundToInt()
            )
        )
    }
}
