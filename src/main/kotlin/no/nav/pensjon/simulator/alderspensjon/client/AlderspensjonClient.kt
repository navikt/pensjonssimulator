package no.nav.pensjon.simulator.alderspensjon.client

import no.nav.pensjon.simulator.alderspensjon.AlderspensjonResult
import no.nav.pensjon.simulator.alderspensjon.AlderspensjonSpec

interface AlderspensjonClient {

    fun simulerAlderspensjon(spec: AlderspensjonSpec): AlderspensjonResult
}
