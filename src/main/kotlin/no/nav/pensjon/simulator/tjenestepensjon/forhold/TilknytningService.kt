package no.nav.pensjon.simulator.tjenestepensjon.forhold

import mu.KotlinLogging
import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.forhold.client.TjenestepensjonsforholdClient
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class TilknytningService(val client: TjenestepensjonsforholdClient) {

    val log = KotlinLogging.logger { }

    fun erPersonTilknyttetTjenestepensjonsordning(pid: Pid, organisasjonsnummer: Organisasjonsnummer): Boolean =
        try {
            client.hentErBrukerTilknyttetTpLeverandoer(pid, organisasjonsnummer)
        } catch (e: EgressException) {
            if (e.statusCode == HttpStatus.NOT_FOUND && e.message == "Person ikke funnet.")
                false.also { log.warn("TPO-tilknytning for PID $pid - ${e.message}") }
            else
                throw e
        }
}