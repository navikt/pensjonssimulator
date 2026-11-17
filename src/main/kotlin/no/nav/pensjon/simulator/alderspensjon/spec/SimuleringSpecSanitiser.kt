package no.nav.pensjon.simulator.alderspensjon.spec

import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.tech.time.DateUtil.foersteDagNesteMaaned
import no.nav.pensjon.simulator.validity.BadSpecException
import java.time.LocalDate

object SimuleringSpecSanitiser {

    fun sanitise(spec: SimuleringSpec): SimuleringSpec {
        val foersteUttakFom: LocalDate = definert(spec.foersteUttakDato)
        val endreFoersteUttak = foersteUttakFom.dayOfMonth != 1
        val endreHeltUttak = spec.heltUttakDato?.let { it.dayOfMonth != 1 } == true

        val nyFoersteUttakFom: LocalDate =
            if (endreFoersteUttak) foersteDagNesteMaaned(foersteUttakFom) else foersteUttakFom

        val nyHeltUttakFom: LocalDate? =
            if (endreHeltUttak) foersteDagNesteMaaned(spec.heltUttakDato) else spec.heltUttakDato

        return if (endreFoersteUttak || endreHeltUttak)
            spec.withUttak(
                foersteUttakDato = nyFoersteUttakFom,
                uttaksgrad = spec.uttakGrad,
                heltUttakDato = nyHeltUttakFom,
                inntektEtterHeltUttakTom = spec.inntektEtterHeltUttakTom
                    ?.coerceAtLeast(nyHeltUttakFom ?: nyFoersteUttakFom)
            )
        else
            spec
    }

    private fun definert(dato: LocalDate?) =
        dato ?: throw BadSpecException("SimuleringSpecSanitiser melder at foersteUttakDato er udefinert")
}