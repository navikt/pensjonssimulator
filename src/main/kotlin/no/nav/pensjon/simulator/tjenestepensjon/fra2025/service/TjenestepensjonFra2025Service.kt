package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service

import mu.KotlinLogging
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjonMedMaanedsUtbetalinger
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.*
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.KlpTjenestepensjonService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.sisteordning.SisteTpOrdningService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.SpkTjenestepensjonService
import no.nav.pensjon.simulator.tpregisteret.TpregisteretClient
import org.springframework.stereotype.Service

@Service
class TjenestepensjonFra2025Service(
    private val tpregisteretClient: TpregisteretClient,
    private val spk: SpkTjenestepensjonService,
    private val klp: KlpTjenestepensjonService,
    private val sisteTpOrdningService: SisteTpOrdningService,
) {
    private val log = KotlinLogging.logger {}

    fun simuler(spec: OffentligTjenestepensjonFra2025SimuleringSpec):
            Pair<List<String>, Result<SimulertTjenestepensjonMedMaanedsUtbetalinger>> {

        val tpOrdninger = try {
            tpregisteretClient.findAlleTpForhold(spec.pid)
        } catch (e: TpregisteretException) {
            return emptyList<String>() to Result.failure(e)
        }

        val tpOrdningerNavn = tpOrdninger.map { it.navn }.sorted()
        val sisteOrdningerNr = sisteTpOrdningService.finnSisteOrdningKandidater(tpOrdninger)

        if (sisteOrdningerNr.isEmpty()) {
            return emptyList<String>() to Result.failure(BrukerErIkkeMedlemException())
        }

        log.info { "Fant TP-ordninger med numre: $sisteOrdningerNr" }

        // Apotekere og brukere fodt for 1963 vil ikke kunne simulere tjenestepensjon enda
        if (spec.gjelderApoteker || spec.foedselsdato.year < 1963) return tpOrdningerNavn to Result.failure(
            TpOrdningStoettesIkkeException("Apoteker")
        )

        val simulertTpListe = sisteOrdningerNr.map { ordning ->
            when (ordning) {
                "3010", "3060" -> spk.simuler(spec, ordning) // TpNummer for SPK
                "4082", "3200" -> klp.simuler(spec, ordning) // TpNummer for KLP
                else -> Result.failure(TpOrdningStoettesIkkeException(ordning))
            }.run {
                onSuccess { return tpOrdningerNavn to this }
                onFailure { if (it is TomSimuleringFraTpOrdningException) return tpOrdningerNavn to this } //Skjer kun hvis siste ordning
            }
        }

        // Returnerer først tekniske feil hvis funnet
        simulertTpListe.forEach { simulering ->
            simulering.onFailure {
                if (it !is IkkeSisteOrdningException && it !is TpOrdningStoettesIkkeException && it !is TomSimuleringFraTpOrdningException) return tpOrdningerNavn to simulering
            }
        }

        log.info { "Ingen simulering fra $tpOrdninger: ${simulertTpListe.map { it.exceptionOrNull()?.message }.joinToString(";")}" }
        return tpOrdningerNavn to simulertTpListe.first()
    }
}
