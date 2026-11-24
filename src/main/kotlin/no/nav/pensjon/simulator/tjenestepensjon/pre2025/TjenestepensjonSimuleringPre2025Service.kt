package no.nav.pensjon.simulator.tjenestepensjon.pre2025

import mu.KotlinLogging
import no.nav.pensjon.simulator.tjenestepensjon.Tjenestepensjonsforhold
import no.nav.pensjon.simulator.tjenestepensjon.Tjenestepensjonsordning
import no.nav.pensjon.simulator.tjenestepensjon.forhold.client.TjenestepensjonsforholdClient
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.SimulerOffentligTjenestepensjonResult.Companion.ikkeMedlem
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.SimulerOffentligTjenestepensjonResult.Companion.tpOrdningStoettesIkke
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.BrukerKvalifisererIkkeTilTjenestepensjonException
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.Pre2025TjenestepensjonService
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.TjenestepensjonSimuleringPre2025Spec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.StillingsprosentService
import org.springframework.stereotype.Component

/**
 * Service for V1 and V2 in TjenestepensjonPre2025Controller.
 */
@Component
class TjenestepensjonSimuleringPre2025Service(
    private val client: TjenestepensjonsforholdClient,
    private val stillingsprosentService: StillingsprosentService,
    private val simuleringService: Pre2025TjenestepensjonService
) {
    val log = KotlinLogging.logger { }

    fun simuler(spec: TjenestepensjonSimuleringPre2025Spec): SimulerOffentligTjenestepensjonResult {
        log.debug { "Simulering av tjenestepensjon pre 2025: ${filterFnr(spec.toString())}" }

        try {
            val pid = spec.pid

            val alleForhold: List<Tjenestepensjonsordning> = client.findAlleTpForhold(pid)
                .mapNotNull(::tpOrdning)

            if (alleForhold.isEmpty()) {
                log.debug { "No TP-forhold found for person" }
                return ikkeMedlem()
            }

            val tpOrdning = alleForhold.firstOrNull { relevanteTpNumre.contains(it.tpNr) }

            if (tpOrdning == null) {
                log.warn { "No supported TP-Ordning found" }
                return tpOrdningStoettesIkke()
            }

            val stillingsprosentListe = stillingsprosentService.getStillingsprosentListe(pid, tpOrdning)

            if (stillingsprosentListe.isEmpty()) {
                log.warn { "No stillingsprosent found" }
                throw RuntimeException("No stillingsprosent found for person")
            }

            log.debug { "Request simulation from $TP_ORDNING_NAVN using REST" }
            val response = simuleringService.simulerOffentligTjenestepensjon(spec, stillingsprosentListe, tpOrdning)
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

    private fun tpOrdning(forhold: Tjenestepensjonsforhold): Tjenestepensjonsordning? =
        client.findTssId(tpNr = forhold.tpNr)?.let {
            Tjenestepensjonsordning(
                navn = forhold.navn,
                tpNr = forhold.tpNr,
                datoSistOpptjening = forhold.datoSistOpptjening,
                tssId = it
            )
        }

    companion object {

        private const val TP_ORDNING_NAVN = "SPK"
        private val relevanteTpNumre = setOf("3010", "3060")

        val FNR_REGEX = """[0-9]{11}""".toRegex()
        fun filterFnr(s: String) = FNR_REGEX.replace(s, "*****")
    }
}