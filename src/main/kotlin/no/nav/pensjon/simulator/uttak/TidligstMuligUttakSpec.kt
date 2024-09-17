package no.nav.pensjon.simulator.uttak

import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.time.DateUtil.foersteDagNesteMaaned
import java.time.LocalDate

data class TidligstMuligUttakSpec(
    val pid: Pid,
    val foedselDato: LocalDate,
    val gradertUttak: GradertUttakSpec?,
    val rettTilOffentligAfpFom: LocalDate?,
    val antallAarUtenlandsEtter16Aar: Int,
    val fremtidigInntektListe: List<InntektSpec>,
    val epsHarPensjon: Boolean,
    val epsHarInntektOver2G: Boolean
) {
    fun sanitise(): TidligstMuligUttakSpec {
        val modifyGradertUttak = gradertUttak?.heltUttakFom?.let { it.dayOfMonth != 1 } ?: false
        val modifyInntekt = fremtidigInntektListe.any { it.fom.dayOfMonth != 1 }

        val newGradertUttak: GradertUttakSpec? = if (modifyGradertUttak) gradertUttak?.sanitise() else gradertUttak
        val newInntektListe: List<InntektSpec> =
            if (modifyInntekt) fremtidigInntektListe.map(InntektSpec::sanitise) else fremtidigInntektListe

        return if (modifyGradertUttak || modifyInntekt)
            TidligstMuligUttakSpec(
                pid,
                foedselDato,
                gradertUttak = newGradertUttak,
                rettTilOffentligAfpFom,
                antallAarUtenlandsEtter16Aar,
                fremtidigInntektListe = newInntektListe,
                epsHarPensjon,
                epsHarInntektOver2G
            )
        else
            this
    }
}

data class GradertUttakSpec(
    val grad: UttakGrad,
    val heltUttakFom: LocalDate
) {
    fun sanitise() =
        if (heltUttakFom.dayOfMonth == 1) this else GradertUttakSpec(grad, foersteDagNesteMaaned(heltUttakFom))
}

data class InntektSpec(
    val fom: LocalDate,
    val aarligBeloep: Int
) {
    fun sanitise() =
        if (fom.dayOfMonth == 1) this else InntektSpec(foersteDagNesteMaaned(fom), aarligBeloep)
}
