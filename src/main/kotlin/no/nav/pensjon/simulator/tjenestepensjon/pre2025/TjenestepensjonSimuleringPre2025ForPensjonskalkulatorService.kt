package no.nav.pensjon.simulator.tjenestepensjon.pre2025

import com.nimbusds.jose.util.JSONObjectUtils
import mu.KotlinLogging
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.TjenestepensjonSimuleringPre2025Spec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.Feilkode
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
import java.time.LocalDate

@Component
class TjenestepensjonSimuleringPre2025ForPensjonskalkulatorService(
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
                return tpOrdningStoettesIkke(alleForhold.map { it.navn })
            }

            if (featureToggleService.isEnabled("mock-offentligtp-foer-1963")) {
                if (spec.pid.value == "19456138058") throw MOCK_SPK_EXCEPTION1
                if (spec.pid.value == "13426143482") throw MOCK_SPK_EXCEPTION2
                if (spec.pid.value == "26456120872") throw MOCK_SPK_EXCEPTION3
                if (spec.pid.value == "25476113736") return MOCK_SPK_RESULT
            }


            val stillingsprosentListe = spkStillingsprosentService.getStillingsprosentListe(pid.value, spkMedlemskap)

            if (stillingsprosentListe.isEmpty()) {
                log.warn { "No stillingsprosent found" }
                return SimulerOffentligTjenestepensjonResultV1(
                    tpnr = spkMedlemskap.tpNr,
                    navnOrdning = spkMedlemskap.navn,
                    feilkode = Feilkode.TEKNISK_FEIL,
                    utbetalingsperiodeListe = emptyList()
                )
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
            log.warn { "Feilmelding i respons fra SPK: ${e}" }

            val feilkode = Feilkode.fromExternalValue(errorCode, errorMessage)

            if (feilkode == Feilkode.BEREGNING_GIR_NULL_UTBETALING)
                return SimulerOffentligTjenestepensjonResultV1(
                    tpnr = spkMedlemskap.tpNr,
                    navnOrdning = spkMedlemskap.navn,
                    feilkode = feilkode,
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
                feilkode = feilkode,
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
        val MOCK_SPK_EXCEPTION3 = EgressException(
            """{"errorCode":"CALC002","message":"Validation problem: Beregning gir 0 i utbetaling."}""",
        )
        val MOCK_SPK_RESULT = SimulerOffentligTjenestepensjonResultV1(
            tpnr = "3010",
            navnOrdning = "Statens pensjonskasse",
            inkluderteOrdningerListe = listOf("Statens pensjonskasse"),
            leverandorUrl = "spk.no",
            utbetalingsperiodeListe = listOf(
                UtbetalingsperiodeV1(
                    uttaksgrad = 2,
                    arligUtbetaling = 6979.0,
                    datoFom = Alder.fromAlder(LocalDate.of(1961,7,25), Alder(64, 5)),
                    datoTom = Alder.fromAlder(LocalDate.of(1961,7,25), Alder(65, 0)),
                    ytelsekode = YtelseCode.AFP
                ),
                UtbetalingsperiodeV1(
                    uttaksgrad = 2,
                    arligUtbetaling = 540000.0,
                    datoFom = Alder.fromAlder(LocalDate.of(1961,7,25), Alder(65, 0)),
                    datoTom = Alder.fromAlder(LocalDate.of(1961,7,25), Alder(67, 0)),
                    ytelsekode = YtelseCode.AFP
                ),
                UtbetalingsperiodeV1(
                    uttaksgrad = 100,
                    arligUtbetaling = 173376.0,
                    datoFom = Alder.fromAlder(LocalDate.of(1961,7,25), Alder(67, 0)),
                    datoTom = Alder.fromAlder(LocalDate.of(1961,7,25), Alder(67, 5)),
                    ytelsekode = YtelseCode.AP
                ),
                UtbetalingsperiodeV1(
                    uttaksgrad = 100,
                    arligUtbetaling = 167244.0,
                    datoFom = Alder.fromAlder(LocalDate.of(1961,7,25), Alder(67, 5)),
                    datoTom = Alder.fromAlder(LocalDate.of(1961,7,25), Alder(68, 5)),
                    ytelsekode = YtelseCode.AP
                ),
                UtbetalingsperiodeV1(
                    uttaksgrad = 100,
                    arligUtbetaling = 163476.0,
                    datoFom = Alder.fromAlder(LocalDate.of(1961,7,25), Alder(68, 5)),
                    datoTom = null,
                    ytelsekode = YtelseCode.AP
                )
            ),
            brukerErIkkeMedlemAvTPOrdning = false,
            brukerErMedlemAvTPOrdningSomIkkeStoettes = false,
        )
    }
}