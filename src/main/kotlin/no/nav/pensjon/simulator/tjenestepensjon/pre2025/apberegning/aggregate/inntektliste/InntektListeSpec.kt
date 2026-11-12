package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.inntektliste

import no.nav.pensjon.simulator.alder.Alder.Companion.fromAlder
import no.nav.pensjon.simulator.opptjening.OpptjeningConstants.hoeyesteAlderForOpptjening
import java.time.LocalDate

data class InntektListeSpec(
    val foedselsdato: LocalDate,
    val inntektFoerFoersteUttak: Int,
    val gradertUttak: Boolean,
    val simuleringTypeErAfpEtterfAlder: Boolean,
    val inntektUnderGradertUttakBeloep: Int,
    val inntektEtterHeltUttakBeloep: Int,
    val inntektEtterHeltUttakTom: LocalDate,
    val foersteUttakDato: LocalDate,
    val heltUttakDato: LocalDate?
) {
    fun inntektTom(): LocalDate {
        val sisteMuligeOpptjeningsdato: LocalDate =
            fromAlder(foedselsdato, alder = hoeyesteAlderForOpptjening)

        return if (inntektEtterHeltUttakBeloep > 0
            && inntektEtterHeltUttakTom.isBefore(sisteMuligeOpptjeningsdato)
        )
            inntektEtterHeltUttakTom
        else
            sisteMuligeOpptjeningsdato
    }
}