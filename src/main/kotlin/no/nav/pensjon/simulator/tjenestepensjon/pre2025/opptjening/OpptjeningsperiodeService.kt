package no.nav.pensjon.simulator.tjenestepensjon.pre2025.opptjening

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.opptjening.error.DuplicateOpptjeningsperiodeEndDateException
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.opptjening.error.MissingOpptjeningsperiodeException
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.OpptjeningsperiodeDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.acl.Stillingsprosent
import no.nav.pensjon.simulator.tpregisteret.acl.TpOrdningFullDto
import no.nav.tjenestepensjon.simulering.v2.service.OpptjeningsperiodeResponse

interface OpptjeningsperiodeService {
    fun getOpptjeningsperiodeListe(tpOrdning: TpOrdningFullDto, stillingsprosentListe: List<Stillingsprosent>): OpptjeningsperiodeResponse
    @Throws(DuplicateOpptjeningsperiodeEndDateException::class, MissingOpptjeningsperiodeException::class)
    fun getLatestFromOpptjeningsperiode(map: Map<TpOrdningFullDto, List<OpptjeningsperiodeDto>>): TpOrdningFullDto
}