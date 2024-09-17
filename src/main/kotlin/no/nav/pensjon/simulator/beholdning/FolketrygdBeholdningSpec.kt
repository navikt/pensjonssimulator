package no.nav.pensjon.simulator.beholdning

import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.time.DateUtil.foersteDagNesteMaaned
import java.time.LocalDate

data class FolketrygdBeholdningSpec(
    val pid: Pid,
    val uttakFom: LocalDate,
    val fremtidigInntektListe: List<InntektSpec> = emptyList(),
    val antallAarUtenlandsEtter16Aar: Int = 0,
    val epsHarPensjon: Boolean = false,
    val epsHarInntektOver2G: Boolean = false
) {
    fun sanitise(): FolketrygdBeholdningSpec {
        val modifyUttakFom = uttakFom.dayOfMonth != 1
        val modifyInntekt = fremtidigInntektListe.any { it.inntektFom.dayOfMonth != 1 }

        val newUttakFom: LocalDate = if (modifyUttakFom) foersteDagNesteMaaned(uttakFom) else uttakFom
        val newInntektListe: List<InntektSpec> =
            if (modifyInntekt) fremtidigInntektListe.map(InntektSpec::sanitise) else fremtidigInntektListe

        return if (modifyUttakFom || modifyInntekt)
            FolketrygdBeholdningSpec(
                pid,
                uttakFom = newUttakFom,
                fremtidigInntektListe = newInntektListe,
                antallAarUtenlandsEtter16Aar,
                epsHarPensjon,
                epsHarInntektOver2G
            )
        else
            this
    }
}

data class InntektSpec(
    val inntektAarligBeloep: Int,
    val inntektFom: LocalDate
) {
    fun sanitise() =
        if (inntektFom.dayOfMonth == 1) this else InntektSpec(inntektAarligBeloep, foersteDagNesteMaaned(inntektFom))
}
