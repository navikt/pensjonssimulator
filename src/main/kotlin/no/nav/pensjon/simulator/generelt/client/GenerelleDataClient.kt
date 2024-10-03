package no.nav.pensjon.simulator.generelt.client

import no.nav.pensjon.simulator.generelt.GenerelleData
import no.nav.pensjon.simulator.generelt.GenerelleDataSpec

interface GenerelleDataClient {
    fun fetchGenerelleData(spec: GenerelleDataSpec): GenerelleData
}
