package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api

import mu.KotlinLogging
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.SimulerOffentligTjenestepensjonResultV1
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.SimulerOffentligTjenestepensjonResultV1.Companion.ikkeMedlem
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.SimulerOffentligTjenestepensjonResultV1.Companion.tpOrdningStoettesIkke
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.BrukerKvalifisererIkkeTilTjenestepensjonException
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.SPKTjenestepensjonServicePre2025
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.HentPrognoseMapper.fromDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.HentPrognoseMapper.toDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.HentPrognoseResponseDto
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.SPKStillingsprosentService
import no.nav.pensjon.simulator.tpregisteret.TPOrdningIdDto
import no.nav.pensjon.simulator.tpregisteret.TpOrdningFullDto
import no.nav.pensjon.simulator.tpregisteret.TpregisteretClient
import org.springframework.stereotype.Component

@Component
class TjenestepensjonSimuleringPre2025Service(
    val tpregisteretClient: TpregisteretClient,
    val spkStillingsprosentService: SPKStillingsprosentService,
    val spkTjenestepensjonServicePre2025: SPKTjenestepensjonServicePre2025,
) {
    val log = KotlinLogging.logger { }

    fun simuler(spec: TjenestepensjonSimuleringPre2025Spec): SimulerOffentligTjenestepensjonResultV1 {
        log.info { "Simulering av tjenestepensjon pre 2025: $spec" }
        //metrics.incrementCounter(APP_NAME, APP_TOTAL_SIMULERING_CALLS)
        try {

            val fnr = spec.pid.value
            val alleForhold: List<TpOrdningFullDto> = tpregisteretClient.findAlleTPForhold(fnr)
                .mapNotNull { forhold ->
                    tpregisteretClient.findTssId(forhold.tpNr)
                        ?.let { TPOrdningIdDto(tpId = forhold.tpNr, tssId = it) }
                        ?.mapTilTpOrdningFullDto(forhold)
                }

            if (alleForhold.isEmpty()) {
                log.debug { "No TP-forhold found for person" }
                return SimulerOffentligTjenestepensjonResultV1.ikkeMedlem()
            }

            val spkMedlemskap = alleForhold.firstOrNull { it.tpNr == "3010" || it.tpNr == "3060" }
            if (spkMedlemskap == null) {
                val firstTPOrdningPaaListen = alleForhold.first()
                val tpNr = firstTPOrdningPaaListen.tpNr
                val name = firstTPOrdningPaaListen.navn
                log.warn { "No supported TP-Ordning found" }
                return SimulerOffentligTjenestepensjonResultV1.tpOrdningStoettesIkke()
            }
            //metrics.incrementCounterWithTag(AppMetrics.Metrics.TP_REQUESTED_LEVERANDOR, "${spkMedlemskap.tpNr} $PROVIDER")

            val stillingsprosentListe = spkStillingsprosentService.getStillingsprosentListe(fnr, spkMedlemskap)

            if (stillingsprosentListe.isEmpty()) {
                log.warn { "No stillingsprosent found" }
                throw RuntimeException("No stillingsprosent found for person")
            }
            //metrics.incrementCounter(APP_NAME, APP_TOTAL_STILLINGSPROSENT_OK)

            log.debug { "Request simulation from SPK using REST" }
            val response: HentPrognoseResponseDto = spkTjenestepensjonServicePre2025.simulerOffentligTjenestepensjon(
                toDto(spec),
                stillingsprosentListe,
                spkMedlemskap,
            )
            //metrics.incrementRestCounter(PROVIDER, "OK")
            log.debug { "Returning response: ${filterFnr(response.toString())}" }
            return fromDto(response)
        } catch (e: BrukerKvalifisererIkkeTilTjenestepensjonException) {
            //metrics.incrementCounter(APP_TOTAL_SIMULERING_BRUKER_KVALIFISERER_IKKE)
            log.warn { "Bruker kvalifiserer ikke til tjenestepensjon. ${e.message}" }
            throw e
        } catch (e: Throwable) {
            log.error(e) { "Unable to simulate offentlig tjenestepensjon pre 2025: ${e.message}" }
            throw e
//                if (e is SimuleringException) {
//                    metrics.incrementCounter(APP_NAME, APP_TOTAL_SIMULERING_FEIL)
//                } else {
//                    metrics.incrementCounter(APP_NAME, APP_TOTAL_STILLINGSPROSENT_ERROR)
//                }
        }
    }

    companion object {
        private val fnrFilterRegex = "(?<=\\d{6})\\d{5}".toRegex()
        fun filterFnr(s: String) = fnrFilterRegex.replace(s, "*****")
    }
}

