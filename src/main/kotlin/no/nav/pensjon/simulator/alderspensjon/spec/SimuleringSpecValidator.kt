package no.nav.pensjon.simulator.alderspensjon.spec

import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.tech.web.BadRequestException

object SimuleringSpecValidator {

    fun validate(spec: SimuleringSpec){
        spec.fremtidigInntektListe?.let(::validateInntekt)
        validateUttak(spec)
    }

    private fun validateInntekt(inntektListe: MutableList<FremtidigInntekt>) {
        if (inntektListe.any { it.fom.dayOfMonth != 1 }) {
            throw BadRequestException("En fremtidig inntekt har f.o.m.-dato som ikke er den 1. i måneden")
        }

        if (inntektListe.groupBy { it.fom }.size < inntektListe.size) {
            throw BadRequestException("To fremtidige inntekter har samme f.o.m.-dato")
        }

        if (inntektListe.any { it.aarligInntektBeloep < 0 }) {
            throw BadRequestException("En fremtidig inntekt har negativt beløp")
        }
    }

    private fun validateUttak(spec: SimuleringSpec) {
        if (spec.foersteUttakDato == null) {
            throw BadRequestException("Dato for første uttak mangler")
        }

        if (spec.heltUttakDato != null) {
            if (spec.foersteUttakDato.isAfter(spec.heltUttakDato))
            throw BadRequestException("Andre uttak (100 %) starter ikke etter første uttak (gradert)")
        }

      //  if (spec.heltUttakDato?.let { spec.foersteUttakDato.isAfter(it).not() } == true) {
        //    throw BadRequestException("Andre uttak (100 %) starter ikke etter første uttak (gradert)")
       // }
    }
}
