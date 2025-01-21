package no.nav.pensjon.simulator.alderspensjon.spec

import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.tech.time.DateUtil.foersteDagNesteMaaned
import java.time.LocalDate

object SimuleringSpecSanitiser {

    fun sanitise(spec: SimuleringSpec): SimuleringSpec {
        val modifyGradertUttak = spec.foersteUttakDato?.let { it.dayOfMonth != 1 } == true
        val modifyHeltUttak = spec.heltUttakDato?.let { it.dayOfMonth != 1 } == true

        val newGradertUttak: LocalDate? = if (modifyGradertUttak) foersteDagNesteMaaned(spec.foersteUttakDato) else spec.foersteUttakDato
        val newHeltUttakFom: LocalDate? = if (modifyHeltUttak) foersteDagNesteMaaned(spec.heltUttakDato) else spec.heltUttakDato

        return if (modifyGradertUttak || modifyHeltUttak)
            spec.withUttak(
                foersteUttakDato = newGradertUttak,
                uttaksgrad = spec.uttakGrad,
                heltUttakDato = newHeltUttakFom,
                inntektEtterHeltUttakAntallAar = spec.inntektEtterHeltUttakAntallAar
            )
        else
            spec
    }
}
