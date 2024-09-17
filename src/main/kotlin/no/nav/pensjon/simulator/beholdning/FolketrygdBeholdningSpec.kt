package no.nav.pensjon.simulator.beholdning

import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.time.DateUtil.foersteDagNesteMaaned
import no.nav.pensjon.simulator.tech.web.BadRequestException
import java.time.LocalDate

data class FolketrygdBeholdningSpec(
    val pid: Pid,
    val uttakFom: LocalDate,
    val fremtidigInntektListe: List<InntektSpec> = emptyList(),
    val antallAarUtenlandsEtter16Aar: Int = 0,
    val epsHarPensjon: Boolean = false,
    val epsHarInntektOver2G: Boolean = false
) {
    fun sanitised(): FolketrygdBeholdningSpec {
        val modifyUttakFom = uttakFom.dayOfMonth != 1
        val newUttakFom: LocalDate = if (modifyUttakFom) foersteDagNesteMaaned(uttakFom) else uttakFom

        return if (modifyUttakFom)
            FolketrygdBeholdningSpec(
                pid,
                uttakFom = newUttakFom,
                fremtidigInntektListe,
                antallAarUtenlandsEtter16Aar,
                epsHarPensjon,
                epsHarInntektOver2G
            )
        else
            this
    }

    fun validated(): FolketrygdBeholdningSpec {
        if (fremtidigInntektListe.any { it.inntektFom.dayOfMonth != 1 }) {
            throw BadRequestException("En fremtidig inntekt har f.o.m.-dato som ikke er den 1. i måneden")
        }

        if (fremtidigInntektListe.groupBy { it.inntektFom }.size < fremtidigInntektListe.size) {
            throw BadRequestException("To fremtidige inntekter har samme f.o.m.-dato")
        }

        if (fremtidigInntektListe.any { it.inntektAarligBeloep < 0 }) {
            throw BadRequestException("En fremtidig inntekt har negativt beløp")
        }

        return this
    }
}

data class InntektSpec(
    val inntektAarligBeloep: Int,
    val inntektFom: LocalDate
)
