package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service

import mu.KotlinLogging
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjonMedMaanedsUtbetalinger
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.*
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.KlpTjenestepensjonService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.sisteordning.SisteTpOrdningService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.SpkTjenestepensjonService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand.StorebrandTjenestepensjonService
import no.nav.pensjon.simulator.tpregisteret.TpForhold
import no.nav.pensjon.simulator.tpregisteret.TpregisteretClient
import org.springframework.stereotype.Service

@Service
class TjenestepensjonFra2025Service(
    private val tpregisteretClient: TpregisteretClient,
    private val spk: SpkTjenestepensjonService,
    private val klp: KlpTjenestepensjonService,
    private val storebrand: StorebrandTjenestepensjonService,
    private val sisteTpOrdningService: SisteTpOrdningService,
) {
    private val log = KotlinLogging.logger {}

    fun simuler(spec: OffentligTjenestepensjonFra2025SimuleringSpec):
            Pair<List<TpForhold>, Result<SimulertTjenestepensjonMedMaanedsUtbetalinger>> {

        val tpOrdninger: List<TpForhold> = try {
            tpregisteretClient.findAlleTpForhold(spec.pid)
        } catch (e: TpregisteretException) {
            return emptyList<TpForhold>() to Result.failure(e)
        }

        val sisteOrdningerNr = sisteTpOrdningService.finnSisteOrdningKandidater(tpOrdninger)

        if (sisteOrdningerNr.isEmpty()) {
            return emptyList<TpForhold>() to Result.failure(BrukerErIkkeMedlemException())
        }

        log.info { "Fant TP-ordninger med numre: $sisteOrdningerNr" }

        val simulertTpListe = sisteOrdningerNr.map { ordning ->
            when (ordning) {
                "3010" -> spk.simuler(spec, ordning) // TpNummer for SPK
                "4082", "3200" -> klp.simuler(spec, ordning) // TpNummer for KLP
                "4081", "4083", "4098" -> storebrand.simuler(spec, ordning) // TpNummer for Storebrand
                else -> Result.failure(TpOrdningStoettesIkkeException(tpOrdninger.joinToString { it.navn }))
            }.run {
                onSuccess { return tpOrdninger to this }
                onFailure { if (it is TomSimuleringFraTpOrdningException) return tpOrdninger to this } //Skjer kun hvis siste ordning
            }
        }

        // Returnerer først tekniske feil hvis funnet
        simulertTpListe.forEach { simulering ->
            simulering.onFailure {
                if (it !is IkkeSisteOrdningException && it !is TpOrdningStoettesIkkeException && it !is TomSimuleringFraTpOrdningException) return tpOrdninger to simulering
            }
        }

        log.info { "Ingen simulering fra $tpOrdninger: ${simulertTpListe.map { it.exceptionOrNull()?.message }.joinToString(";")}" }
        return tpOrdninger to simulertTpListe.first()
    }
}
