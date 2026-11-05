package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent

import mu.KotlinLogging
import no.nav.pensjon.simulator.person.FoedselsnummerUtil.redact
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.Tjenestepensjonsordning
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.StillingsprosentClient
import org.springframework.stereotype.Service

@Service
class SpkStillingsprosentService(private val client: StillingsprosentClient) {

    private val log = KotlinLogging.logger {}

    fun getStillingsprosentListe(pid: Pid, tpOrdning: Tjenestepensjonsordning): List<Stillingsprosent> =
        try {
            client.getStillingsprosenter(pid, tpOrdning)
        } catch (e: EgressException) {
            log.warn { "Failed to fetch stillingsprosenter: ${redact(e.message)}" }
            emptyList()
        }
}