package no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent

import mu.KotlinLogging
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.acl.Stillingsprosent
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.client.SPKStillingsprosentSoapClient
import no.nav.pensjon.simulator.tpregisteret.TpOrdningFullDto
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

@Service
class SPKStillingsprosentService(
    private val client: SPKStillingsprosentSoapClient,
    //private val metrics: AppMetrics
) {
    private val log = KotlinLogging.logger {}

    fun getStillingsprosentListe(fnr: String, tpOrdning: TpOrdningFullDto): List<Stillingsprosent> {
//        metrics.incrementCounter(AppMetrics.Metrics.APP_NAME, AppMetrics.Metrics.APP_TOTAL_OPPTJENINGSPERIODE_CALLS)
        var stillingsprosentList: List<Stillingsprosent> = emptyList()
        val elapsed = measureTimeMillis { stillingsprosentList = client.getStillingsprosenter(fnr, tpOrdning) }
        log.info { "Executed call to stillingsprosenter in: $elapsed ms $stillingsprosentList" }
//        metrics.incrementCounter(AppMetrics.Metrics.APP_NAME, AppMetrics.Metrics.APP_TOTAL_OPPTJENINGSPERIODE_TIME, elapsed.toDouble())
        return stillingsprosentList
    }
}