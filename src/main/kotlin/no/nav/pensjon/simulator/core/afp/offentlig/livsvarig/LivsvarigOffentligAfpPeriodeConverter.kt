package no.nav.pensjon.simulator.core.afp.offentlig.livsvarig

import no.nav.pensjon.simulator.core.out.OutputLivsvarigOffentligAfp
import kotlin.math.roundToInt

object LivsvarigOffentligAfpPeriodeConverter {

    private const val MAANEDER_PER_AAR = 12
    private const val MIN_ANTALL_MAANEDER_FRA_IVERKSETTELSE_TIL_UTTAK = 1

    fun konverterTilArligeAfpOffentligLivsvarigPerioder(
        result: LivsvarigOffentligAfpResult?,
        foedselMaaned: Int?
    ): List<OutputLivsvarigOffentligAfp> {
        if (result == null || result.afpYtelseListe.isEmpty() || foedselMaaned == null) {
            return emptyList()
        }

        val ytelseListe = result.afpYtelseListe
        val ytelseVedUttak = ytelseListe[0]
        val uttakMaaned = ytelseVedUttak.gjelderFom.monthValue

        return when {
            ytelseListe.size > 1 -> {
                val maanederMedUttaksSats =
                    MAANEDER_PER_AAR - uttakMaaned + 1 // uttakMaaned skal inkluderes i antall maaneder med uttakssats
                opprettPerioder(ytelseListe[0], maanederMedUttaksSats, ytelseListe[1], foedselMaaned)
            }

            ytelseVedUttak.gjelderFomAlder.maaneder > 0 -> {
                val maanederMedUttaksSats = maanederFraFoersteUttakTilFyltAar(uttakMaaned, foedselMaaned)
                opprettPerioder(ytelseListe[0], maanederMedUttaksSats, ytelseListe[0], 0)
            }

            else -> listOf(
                OutputLivsvarigOffentligAfp(
                    alderAar = ytelseVedUttak.gjelderFomAlder.aar,
                    beloep = ytelseVedUttak.afpYtelsePerAar.toInt()
                )
            )
        }
    }

    fun maanederFraFoersteUttakTilFyltAar(uttakMaaned: Int, foedselMaaned: Int): Int {
        val uttakMaanedVedUttaksalderMedHeleAar =
            foedselMaaned + MIN_ANTALL_MAANEDER_FRA_IVERKSETTELSE_TIL_UTTAK

        return when {
            uttakMaaned == uttakMaanedVedUttaksalderMedHeleAar -> MAANEDER_PER_AAR
            uttakMaaned > uttakMaanedVedUttaksalderMedHeleAar -> MAANEDER_PER_AAR - uttakMaaned + 1 + foedselMaaned  // uttakMaaned skal inkluderes i antall maaneder med uttakssats. Periode: [uttakMaaned...aarskifte] + [aarskifte...foedselssmaaned]
            else -> uttakMaanedVedUttaksalderMedHeleAar - uttakMaaned // periode: [uttakMaaned...uttakMaanedVedUttaksalderMedHeleAr]
        }
    }

    fun opprettPerioder(
        ytelseVedUttak: LivsvarigOffentligAfpYtelseMedDelingstall,
        maanederMedUttakSats: Int,
        ytelseVedAndreAaret: LivsvarigOffentligAfpYtelseMedDelingstall,
        maanederMedOppdatertUttakSats: Int,
    ): List<OutputLivsvarigOffentligAfp> {
        val alderAarVedUttak = ytelseVedUttak.gjelderFomAlder.aar
        val satsPerMaanedVedUttak = ytelseVedUttak.afpYtelsePerAar / MAANEDER_PER_AAR
        val ytelsePerAarEtterOppdatering = ytelseVedAndreAaret.afpYtelsePerAar
        val satsPerMaanedEtterOppdatering = ytelsePerAarEtterOppdatering / MAANEDER_PER_AAR
        val belop =
            satsPerMaanedVedUttak * maanederMedUttakSats + satsPerMaanedEtterOppdatering * maanederMedOppdatertUttakSats

        return listOf(
            OutputLivsvarigOffentligAfp(alderAar = alderAarVedUttak, beloep = belop.roundToInt()),
            OutputLivsvarigOffentligAfp(
                alderAar = alderAarVedUttak + 1,
                beloep = ytelsePerAarEtterOppdatering.roundToInt()
            )
        )
    }
}
