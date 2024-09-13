package no.nav.pensjon.simulator.beholdning

import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

data class FolketrygdBeholdningSpec(
    val pid: Pid,
    val uttakFom: LocalDate,
    val fremtidigInntektListe: List<InntektSpec> = emptyList(),
    val antallAarUtenlandsEtter16Aar: Int = 0,
    val epsHarPensjon: Boolean = false,
    val epsHarInntektOver2G: Boolean = false
) {
    fun withUttakFom(dato: LocalDate) =
        FolketrygdBeholdningSpec(
            pid,
            uttakFom = dato,
            fremtidigInntektListe,
            antallAarUtenlandsEtter16Aar,
            epsHarPensjon,
            epsHarInntektOver2G
        )
}

data class InntektSpec(
    val inntektAarligBeloep: Int,
    val inntektFom: LocalDate
)
