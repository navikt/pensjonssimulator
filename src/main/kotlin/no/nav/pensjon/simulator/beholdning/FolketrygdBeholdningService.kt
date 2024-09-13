package no.nav.pensjon.simulator.beholdning

import no.nav.pensjon.simulator.beholdning.client.BeholdningClient
import no.nav.pensjon.simulator.tech.time.DateUtil.foersteDagNesteMaaned
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component

class FolketrygdBeholdningService(private val client: BeholdningClient) {

    fun simulerFolketrygdBeholdning(spec: FolketrygdBeholdningSpec): FolketrygdBeholdning =
        client.simulerFolketrygdBeholdning(sanitise(spec))


    private fun sanitise(spec: FolketrygdBeholdningSpec): FolketrygdBeholdningSpec {
        val heltUttakFom: LocalDate = spec.uttakFom

        return if (heltUttakFom.dayOfMonth != 1) {
            spec.withUttakFom(
                dato = foersteDagNesteMaaned(heltUttakFom)
            )
        } else spec
    }
}
