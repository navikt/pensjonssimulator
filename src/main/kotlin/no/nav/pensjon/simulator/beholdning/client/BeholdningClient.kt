package no.nav.pensjon.simulator.beholdning.client

import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagResult
import no.nav.pensjon.simulator.beholdning.BeholdningerMedGrunnlagSpec
import no.nav.pensjon.simulator.beholdning.FolketrygdBeholdning
import no.nav.pensjon.simulator.beholdning.FolketrygdBeholdningSpec

interface BeholdningClient {

    fun fetchBeholdningerMedGrunnlag(spec: BeholdningerMedGrunnlagSpec): BeholdningerMedGrunnlagResult

    fun simulerFolketrygdBeholdning(spec: FolketrygdBeholdningSpec): FolketrygdBeholdning
}
