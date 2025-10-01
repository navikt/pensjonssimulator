package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service

import mu.KotlinLogging
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.BrukerErIkkeMedlemException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TomSimuleringFraTpOrdningException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TpOrdningStoettesIkkeException
import no.nav.pensjon.simulator.tpregisteret.TpregisteretClient
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjonMedMaanedsUtbetalinger
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerTjenestepensjonRequestDto
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.IkkeSisteOrdningException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.exception.TpregisteretException
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.KLPTjenestepensjonService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.sisteordning.FinnSisteTpOrdningService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.SPKTjenestepensjonService
import org.springframework.stereotype.Service

@Service
class TjenestepensjonFra2025Service(
    private val tpregisteretClient: TpregisteretClient,
    private val spk: SPKTjenestepensjonService,
    private val klp: KLPTjenestepensjonService,
    private val finnSisteTpOrdningService: FinnSisteTpOrdningService,
    ) {
    private val log = KotlinLogging.logger {}

    fun simuler(request: SimulerTjenestepensjonRequestDto): Pair<List<String>, Result<SimulertTjenestepensjonMedMaanedsUtbetalinger>> {
        val tpOrdninger = try {
            tpregisteretClient.findAlleTpForhold(request.pid)
        }
        catch (e: TpregisteretException) {
            return emptyList<String>() to Result.failure(e)
        }


        val tpOrdningerNavn = tpOrdninger.map { it.navn }

        val sisteOrdningerNr = finnSisteTpOrdningService.finnSisteOrdningKandidater(tpOrdninger)
        if (sisteOrdningerNr.isEmpty()) {
            return emptyList<String>() to Result.failure(BrukerErIkkeMedlemException())
        }

        log.info { "Fant tp ordninger med nummere: $sisteOrdningerNr" }

        // Apotekere og brukere fodt for 1963 vil ikke kunne simulere tjenestepensjon enda
        if (request.erApoteker || request.foedselsdato.year < 1963) return tpOrdningerNavn to Result.failure(
            TpOrdningStoettesIkkeException("Apoteker")
        )

        val simulertTpListe = sisteOrdningerNr.map { ordning ->
            when (ordning) {
                "3010", "3060" -> spk.simuler(request, ordning) // TpNummer for SPK
                "4082", "3200" -> klp.simuler(request, ordning) // TpNummer for KLP
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

        log.info { "Ingen simulering fra ${tpOrdninger}: ${simulertTpListe.map { it.exceptionOrNull()?.message }.joinToString(";")}" }
        return tpOrdningerNavn to simulertTpListe.first()
    }
}
