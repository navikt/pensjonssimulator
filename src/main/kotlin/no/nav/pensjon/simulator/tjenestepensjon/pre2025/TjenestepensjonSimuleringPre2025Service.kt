package no.nav.pensjon.simulator.tjenestepensjon.pre2025

import com.nimbusds.jose.util.JSONObjectUtils
import mu.KotlinLogging
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.TjenestepensjonSimuleringPre2025Spec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.Feilkode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.SimulerOFTPErrorResponseV1
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.SimulerOffentligTjenestepensjonResultV1
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.SimulerOffentligTjenestepensjonResultV1.*
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.SimulerOffentligTjenestepensjonResultV1.Companion.ikkeMedlem
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.SimulerOffentligTjenestepensjonResultV1.Companion.tpOrdningStoettesIkke
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
    private val featureToggleService: FeatureToggleService
) {
    val log = KotlinLogging.logger { }

    fun simuler(spec: TjenestepensjonSimuleringPre2025Spec): SimulerOffentligTjenestepensjonResultV1 {
        log.info { "Simulering av tjenestepensjon pre 2025: ${filterFnr(spec.toString())}" }
        var spkMedlemskap: TpOrdningFullDto? = null

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

            spkMedlemskap = alleForhold.firstOrNull { it.tpNr == "3010" || it.tpNr == "3060" }
            if (spkMedlemskap == null) {
                log.warn { "No supported TP-Ordning found" }
                return tpOrdningStoettesIkke()
            }

            if (featureToggleService.isEnabled("mock-offentligtp-foer-1963")) {
                if (spec.pid.value == "19456138058") throw MOCK_SPK_EXCEPTION1
                if (spec.pid.value == "13426143482") throw MOCK_SPK_EXCEPTION2
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
        } catch (e: EgressException) {
            val errorCode = JSONObjectUtils.parse(e.message)["errorCode"]?.toString()
            val errorMessage = JSONObjectUtils.parse(e.message)["message"]?.toString()
            if (spkMedlemskap == null || errorCode == null || errorMessage == null) throw e

            val feilkode = Feilkode.fromExternalValue(errorCode, errorMessage)

            if (feilkode == Feilkode.BEREGNING_GIR_NULL_UTBETALING)
                return SimulerOffentligTjenestepensjonResultV1(
                    tpnr = spkMedlemskap.tpNr,
                    navnOrdning = spkMedlemskap.navn,
                    errorResponse = SimulerOFTPErrorResponseV1(
                        errorCode = feilkode,
                        errorMessage = errorMessage
                    ),
                    utbetalingsperiodeListe = listOf(UtbetalingsperiodeV1(
                        datoFom = Alder.fromAlder(spec.foedselsdato, Alder(65,0)),
                        datoTom = Alder.fromAlder(spec.foedselsdato, Alder(67,0)),
                        uttaksgrad = 100,
                        arligUtbetaling = 0.0,
                        ytelsekode = YtelseCode.AFP
                    ),
                    UtbetalingsperiodeV1(
                        datoFom = Alder.fromAlder(spec.foedselsdato, Alder(67,0)),
                        datoTom = null,
                        uttaksgrad = 100,
                        arligUtbetaling = 0.0,
                        ytelsekode = YtelseCode.AP
                    )
                ))

            return SimulerOffentligTjenestepensjonResultV1(
                tpnr = spkMedlemskap.tpNr,
                navnOrdning = spkMedlemskap.navn,
                errorResponse = SimulerOFTPErrorResponseV1(
                    errorCode = feilkode,
                    errorMessage = errorMessage
                ),
                utbetalingsperiodeListe = emptyList()
            )
        }
        catch (e: Throwable) {
            log.error(e) { "Unable to simulate offentlig tjenestepensjon pre 2025: ${e.message}" }
            throw e
        }
    }

    companion object {
        val FNR_REGEX = """[0-9]{11}""".toRegex()
        fun filterFnr(s: String) = FNR_REGEX.replace(s, "*****")

        val MOCK_SPK_EXCEPTION1 = EgressException(
            """{"errorCode":"CALC002","message":"Validation problem: Tjenestetid mindre enn 3 år."}""",
        )
        val MOCK_SPK_EXCEPTION2 = EgressException(
            """{"errorCode":"CALC002","message":"Validation problem: Flere samtidige stillinger er ikke støttet."}""",
        )
    }
}