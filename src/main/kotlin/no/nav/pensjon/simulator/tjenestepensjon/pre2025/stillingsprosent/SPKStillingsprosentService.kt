package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent

import mu.KotlinLogging
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.acl.Stillingsprosent
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.SPKStillingsprosentSoapClient
import no.nav.pensjon.simulator.tpregisteret.TpOrdning
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

@Service
class SPKStillingsprosentService(private val client: SPKStillingsprosentSoapClient) {

    private val log = KotlinLogging.logger {}

    fun getStillingsprosentListe(pid: Pid, tpOrdning: TpOrdning): List<Stillingsprosent> {
        var stillingsprosentListe: List<Stillingsprosent> = emptyList()

        try {
            val elapsed = measureTimeMillis { stillingsprosentListe = client.getStillingsprosenter(pid, tpOrdning) }
            log.info { "Executed call to stillingsprosenter in: $elapsed ms $stillingsprosentListe" }
        } catch (e: EgressException) {
            log.warn { "Failed to fetch stillingsprosenter: ${e.message}" }
        }

        return stillingsprosentListe
    }
}