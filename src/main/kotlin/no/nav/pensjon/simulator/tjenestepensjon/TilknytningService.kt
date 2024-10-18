package no.nav.pensjon.simulator.tjenestepensjon

import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tpregisteret.TpregisteretClient
import org.springframework.stereotype.Service

@Service
class TilknytningService(val client: TpregisteretClient) {

    fun erPersonTilknyttetTjenestepensjonsordning(pid: Pid, organisasjonsnummer: Organisasjonsnummer): Boolean =
        client.hentErBrukerTilknyttetTpLeverandoer(pid, organisasjonsnummer)
}
