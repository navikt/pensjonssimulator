package no.nav.pensjon.simulator.alderspensjon

import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.time.DateUtil.foersteDagNesteMaaned
import no.nav.pensjon.simulator.tech.web.BadRequestException
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
    fun sanitised(): AlderspensjonSpec {
        val modifyGradertUttak = gradertUttak?.fom?.let { it.dayOfMonth != 1 } ?: false
        val modifyHeltUttak = heltUttakFom.dayOfMonth != 1

        val newGradertUttak: GradertUttakSpec? = if (modifyGradertUttak) gradertUttak?.sanitise() else gradertUttak
        val newHeltUttakFom: LocalDate = if (modifyHeltUttak) foersteDagNesteMaaned(heltUttakFom) else heltUttakFom

        return if (modifyGradertUttak || modifyHeltUttak)
            AlderspensjonSpec(
                pid,
                gradertUttak = newGradertUttak,
                heltUttakFom = newHeltUttakFom,
                antallAarUtenlandsEtter16Aar,
                epsHarPensjon,
                epsHarInntektOver2G,
                fremtidigInntektListe,
                rettTilAfpOffentligDato
            )
        else
            this
    }

    fun validated(): AlderspensjonSpec = inntektValidated().uttakValidated()

    private fun inntektValidated(): AlderspensjonSpec {
        if (fremtidigInntektListe.any { it.fom.dayOfMonth != 1 }) {
            throw BadRequestException("En fremtidig inntekt har f.o.m.-dato som ikke er den 1. i måneden")
        }

        if (fremtidigInntektListe.groupBy { it.fom }.size < fremtidigInntektListe.size) {
            throw BadRequestException("To fremtidige inntekter har samme f.o.m.-dato")
        }

        if (fremtidigInntektListe.any { it.aarligBeloep < 0 }) {
            throw BadRequestException("En fremtidig inntekt har negativt beløp")
        }

        return this
    }

    private fun uttakValidated(): AlderspensjonSpec {
        if (gradertUttak?.let { !heltUttakFom.isAfter(it.fom) } == true) {
            throw BadRequestException("Helt uttak starter ikke etter gradert uttak")
        }

        return this
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
)
