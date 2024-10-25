package no.nav.pensjon.simulator.alderspensjon.client

import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.AlderspensjonResult
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.AlderspensjonSpec

interface AlderspensjonClient {

    fun simulerAlderspensjon(spec: AlderspensjonSpec): AlderspensjonResult
}
