package no.nav.pensjon.simulator.uttak

import no.nav.pensjon.simulator.tech.time.DateUtil.foersteDagNesteMaaned
import no.nav.pensjon.simulator.uttak.client.UttakClient
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class UttakService(private val client: UttakClient) {

    fun finnTidligstMuligUttak(spec: TidligstMuligUttakSpec): TidligstMuligUttak =
        client.finnTidligstMuligUttak(sanitise(spec))

    private fun sanitise(spec: TidligstMuligUttakSpec): TidligstMuligUttakSpec {
        val heltUttakFom: LocalDate? = spec.gradertUttak?.heltUttakFom
        val modify = heltUttakFom?.let { it.dayOfMonth != 1 } ?: false

        return if (modify) {
            spec.withHeltUttakFom(
                dato = heltUttakFom?.let(::foersteDagNesteMaaned),
            )
        } else spec
    }
}
