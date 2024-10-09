package no.nav.pensjon.simulator.tpregisteret

import org.springframework.stereotype.Component

@Component
class TpregisteretService(val client: TpregisteretClient) {

    fun erBrukerTilknyttetAngittTpLeverandoer(pid: String, orgNummer: String): Boolean {
        return client.hentErBrukerTilknyttetTpLeverandoer(pid, orgNummer)
    }
}