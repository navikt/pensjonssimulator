package no.nav.pensjon.simulator.beholdning.client

import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagResult
import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagSpec

interface BeholdningClient {

    fun fetchBeholdningerMedGrunnlag(spec: BeholdningerMedGrunnlagSpec): BeholdningerMedGrunnlagResult
}
