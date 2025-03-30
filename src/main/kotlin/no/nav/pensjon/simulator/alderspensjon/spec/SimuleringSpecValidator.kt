package no.nav.pensjon.simulator.alderspensjon.spec

import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.tech.web.BadRequestException
import java.time.LocalDate

object SimuleringSpecValidator {

    fun validate(spec: SimuleringSpec, today: LocalDate) {
        validateInntekt(spec.fremtidigInntektListe)
        validateUttak(spec, today)
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

    private fun validateUttak(spec: SimuleringSpec, today: LocalDate) {
        spec.foersteUttakDato?.let {
            if (it.isBefore(today))
                throw BadRequestException("Dato for første uttak er for tidlig")
        } ?: throw BadRequestException("Dato for første uttak mangler")

        if (spec.heltUttakDato != null) {
            if (spec.foersteUttakDato.isAfter(spec.heltUttakDato))
                throw BadRequestException("Andre uttak (100 %) starter ikke etter første uttak (gradert)")
        }
    }
}
