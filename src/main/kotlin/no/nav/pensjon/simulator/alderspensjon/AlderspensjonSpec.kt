package no.nav.pensjon.simulator.alderspensjon

import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.time.DateUtil.foersteDagNesteMaaned
import java.time.LocalDate

data class AlderspensjonSpec(
    val pid: Pid,
    val gradertUttak: GradertUttakSpec?,
    val heltUttakFom: LocalDate,
    val antallAarUtenlandsEtter16Aar: Int,
    val epsHarPensjon: Boolean,
    val epsHarInntektOver2G: Boolean,
    val fremtidigInntektListe: List<InntektSpec>,
    val rettTilAfpOffentligDato: LocalDate?
) {
    fun sanitise(): AlderspensjonSpec {
        val modifyGradertUttak = gradertUttak?.fom?.let { it.dayOfMonth != 1 } ?: false
        val modifyHeltUttak = heltUttakFom.dayOfMonth != 1
        val modifyInntekt = fremtidigInntektListe.any { it.fom.dayOfMonth != 1 }

        val newGradertUttak: GradertUttakSpec? = if (modifyGradertUttak) gradertUttak?.sanitise() else gradertUttak
        val newHeltUttakFom: LocalDate = if (modifyHeltUttak) foersteDagNesteMaaned(heltUttakFom) else heltUttakFom
        val newInntektListe: List<InntektSpec> =
            if (modifyInntekt) fremtidigInntektListe.map(InntektSpec::sanitise) else fremtidigInntektListe

        return if (modifyGradertUttak || modifyHeltUttak || modifyInntekt)
            AlderspensjonSpec(
                pid,
                gradertUttak = newGradertUttak,
                heltUttakFom = newHeltUttakFom,
                antallAarUtenlandsEtter16Aar,
                epsHarPensjon,
                epsHarInntektOver2G,
                fremtidigInntektListe = newInntektListe,
                rettTilAfpOffentligDato
            )
        else
            this
    }
}

data class GradertUttakSpec(
    val uttaksgrad: Uttaksgrad,
    val fom: LocalDate
) {
    fun sanitise() =
        if (fom.dayOfMonth == 1) this else GradertUttakSpec(uttaksgrad, foersteDagNesteMaaned(fom))
}

data class InntektSpec(
    val aarligBeloep: Int,
    val fom: LocalDate
) {
    fun sanitise() =
        if (fom.dayOfMonth == 1) this else InntektSpec(aarligBeloep, foersteDagNesteMaaned(fom))
}
