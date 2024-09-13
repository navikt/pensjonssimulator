package no.nav.pensjon.simulator.alderspensjon

import no.nav.pensjon.simulator.alderspensjon.client.AlderspensjonClient
import no.nav.pensjon.simulator.tech.time.DateUtil.foersteDagNesteMaaned
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component

class AlderspensjonService(private val client: AlderspensjonClient) {

    fun simulerAlderspensjon(spec: AlderspensjonSpec): AlderspensjonResult =
        client.simulerAlderspensjon(sanitise(spec))

    private fun sanitise(spec: AlderspensjonSpec): AlderspensjonSpec {
        val gradertUttakFom: LocalDate? = spec.gradertUttak?.fom
        val heltUttakFom: LocalDate = spec.heltUttakFom
        val modifyGradert = gradertUttakFom?.let { it.dayOfMonth != 1 } ?: false
        val modifyHelt = heltUttakFom.dayOfMonth != 1

        return if (modifyGradert || modifyHelt) {
            spec.withUttakFom(
                gradertFom = if (modifyGradert) foersteDagNesteMaaned(gradertUttakFom!!) else gradertUttakFom,
                heltFom = if (modifyHelt) foersteDagNesteMaaned(heltUttakFom) else heltUttakFom,
            )
        } else spec
    }
}
