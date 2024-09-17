package no.nav.pensjon.simulator.uttak

import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.time.DateUtil.foersteDagNesteMaaned
import no.nav.pensjon.simulator.tech.web.BadRequestException
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
    fun sanitised(): TidligstMuligUttakSpec {
        val modifyGradertUttak = gradertUttak?.heltUttakFom?.let { it.dayOfMonth != 1 } ?: false
        val newGradertUttak: GradertUttakSpec? = if (modifyGradertUttak) gradertUttak?.sanitise() else gradertUttak

        return if (modifyGradertUttak)
            TidligstMuligUttakSpec(
                pid,
                foedselDato,
                gradertUttak = newGradertUttak,
                rettTilOffentligAfpFom,
                antallAarUtenlandsEtter16Aar,
                fremtidigInntektListe,
                epsHarPensjon,
                epsHarInntektOver2G
            )
        else
            this
    }

    fun validated(): TidligstMuligUttakSpec {
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
)
