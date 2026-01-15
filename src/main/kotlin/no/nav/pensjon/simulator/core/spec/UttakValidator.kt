package no.nav.pensjon.simulator.core.spec

import no.nav.pensjon.simulator.validity.BadSpecException

object UttakValidator {

    fun validateGradertUttak(spec: SimuleringSpec) {
        if (spec.foersteUttakDato == null) {
            throw BadSpecException("dato for første uttak mangler")
        }

        if (spec.heltUttakDato == null) {
            throw BadSpecException("dato for helt uttak mangler (obligatorisk ved gradert uttak)")
        }

        if (spec.foersteUttakDato.isBefore(spec.heltUttakDato).not()) {
            throw BadSpecException(
                "dato for første uttak (${spec.foersteUttakDato}) er ikke før" +
                        " dato for helt uttak (${spec.heltUttakDato})"
            )
        }
    }
}
