package no.nav.pensjon.simulator.tjenestepensjon.forhold.client

import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tjenestepensjon.Tjenestepensjonsforhold

interface TjenestepensjonsforholdClient {
    fun hentErBrukerTilknyttetTpLeverandoer(pid: Pid, organisasjonsnummer: Organisasjonsnummer): Boolean

    fun findAlleTpForhold(pid: Pid): List<Tjenestepensjonsforhold>

    fun findTssId(tpNr: String): String?
}