package no.nav.pensjon.simulator.tech.sporing.client

import no.nav.pensjon.simulator.tech.sporing.Sporing

interface SporingsloggClient {
    fun log(sporing: Sporing)
}
