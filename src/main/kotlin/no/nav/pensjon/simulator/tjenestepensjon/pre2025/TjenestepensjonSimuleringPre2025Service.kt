package no.nav.pensjon.simulator.tjenestepensjon.pre2025

import mu.KotlinLogging
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.SimulerOffentligTjenestepensjonResult.Companion.ikkeMedlem
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.SimulerOffentligTjenestepensjonResult.Companion.tpOrdningStoettesIkke
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.TjenestepensjonSimuleringPre2025Spec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.BrukerKvalifisererIkkeTilTjenestepensjonException
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.SPKTjenestepensjonServicePre2025
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.SPKStillingsprosentService
import no.nav.pensjon.simulator.tpregisteret.TpregisteretClient
import no.nav.pensjon.simulator.tpregisteret.TPOrdningIdDto
import no.nav.pensjon.simulator.tpregisteret.TpOrdningFullDto
import org.springframework.stereotype.Component

@Component
class TjenestepensjonSimuleringPre2025Service(
    val tpregisteretClient: TpregisteretClient,
    val spkStillingsprosentService: SPKStillingsprosentService,
    val spkTjenestepensjonServicePre2025: SPKTjenestepensjonServicePre2025,
) {
    val log = KotlinLogging.logger { }

    fun simuler(spec: TjenestepensjonSimuleringPre2025Spec): SimulerOffentligTjenestepensjonResult {
        log.info { "Simulering av tjenestepensjon pre 2025: ${filterFnr(spec.toString())}" }

        try {
            val pid = spec.pid
            val alleForhold: List<TpOrdningFullDto> = tpregisteretClient.findAlleTpForhold(pid)
                .mapNotNull { forhold ->
                    tpregisteretClient.findTssId(forhold.tpNr)
                        ?.let { TPOrdningIdDto(tpId = forhold.tpNr, tssId = it) }
                        ?.mapTilTpOrdningFullDto(forhold)
                }

            if (alleForhold.isEmpty()) {
                log.debug { "No TP-forhold found for person" }
                return ikkeMedlem()
            }

            val spkMedlemskap = alleForhold.firstOrNull { it.tpNr == "3010" || it.tpNr == "3060" }
            if (spkMedlemskap == null) {
                log.warn { "No supported TP-Ordning found" }
                return tpOrdningStoettesIkke()
            }

            val stillingsprosentListe = spkStillingsprosentService.getStillingsprosentListe(pid.value, spkMedlemskap)

            if (stillingsprosentListe.isEmpty()) {
                log.warn { "No stillingsprosent found" }
                throw RuntimeException("No stillingsprosent found for person")
            }

            log.debug { "Request simulation from SPK using REST" }
            val response = spkTjenestepensjonServicePre2025.simulerOffentligTjenestepensjon(
                spec,
                stillingsprosentListe,
                spkMedlemskap,
            )
            log.debug { "Returning response: ${filterFnr(response.toString())}" }
            return response
        } catch (e: BrukerKvalifisererIkkeTilTjenestepensjonException) {
            log.warn { "Bruker kvalifiserer ikke til tjenestepensjon. ${e.message}" }
            throw e
        } catch (e: Throwable) {
            log.error(e) { "Unable to simulate offentlig tjenestepensjon pre 2025: ${e.message}" }
            throw e
        }
    }

    companion object {
        val FNR_REGEX = """[0-9]{11}""".toRegex()
        fun filterFnr(s: String) = FNR_REGEX.replace(s, "*****")
    }
}