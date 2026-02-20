package no.nav.pensjon.simulator.tjenestepensjon.pre2025

import com.nimbusds.jose.util.JSONObjectUtils
import mu.KotlinLogging
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.tech.env.EnvironmentUtil
import no.nav.pensjon.simulator.tech.metric.Metrics
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService.Companion.PEK_1490_TP_FOER_1963
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.SimulerOffentligTjenestepensjonResult.Companion.ikkeMedlem
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.SimulerOffentligTjenestepensjonResult.Companion.tpOrdningStoettesIkke
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.metrics.SPKResultatKodePre2025.*
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.metrics.SPKResultatKodePre2025.Companion.fromFeilkode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.SPKTjenestepensjonServicePre2025
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.TjenestepensjonSimuleringPre2025Spec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.SPKStillingsprosentService
import no.nav.pensjon.simulator.tpregisteret.TPOrdningIdDto
import no.nav.pensjon.simulator.tpregisteret.TpOrdningFullDto
import no.nav.pensjon.simulator.tpregisteret.TpregisteretClient
import org.apache.el.parser.ParseException
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

    fun simuler(spec: TjenestepensjonSimuleringPre2025Spec): SimulerOffentligTjenestepensjonResult {
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

            if (EnvironmentUtil.isDevelopment() && featureToggleService.isEnabled(PEK_1490_TP_FOER_1963)) {
                if (spec.pid.value == "19456138058") throw MOCK_SPK_EXCEPTION1
                if (spec.pid.value == "13426143482") throw MOCK_SPK_EXCEPTION2
                if (spec.pid.value == "26456120872") throw MOCK_SPK_EXCEPTION3
                if (spec.pid.value == "25476113736") return MOCK_SPK_RESULT
            }


            val stillingsprosentListe = spkStillingsprosentService.getStillingsprosentListe(pid.value, spkMedlemskap)

            if (stillingsprosentListe.isEmpty()) {
                log.warn { "No stillingsprosent found" }
                    .also { Metrics.countTjenestepensjonSimuleringPre2025(INGEN_STILLINGSPROSENT) }

                return SimulerOffentligTjenestepensjonResult(
                    spkMedlemskap.tpNr,
                    spkMedlemskap.navn,
                    emptyList(),
                    feilkode = Feilkode.TEKNISK_FEIL,
                    relevanteTpOrdninger = alleForhold.map { it.navn }
                )
            }

            log.debug { "Request simulation from SPK using REST" }
            val response = spkTjenestepensjonServicePre2025.simulerOffentligTjenestepensjon(
                spec,
                stillingsprosentListe,
                spkMedlemskap,
            ).also { Metrics.countTjenestepensjonSimuleringPre2025(OK) }

            log.debug { "Returning response: ${filterFnr(response.toString())}" }
            return response
        } catch (e: EgressException) {
            val errorCode: String?
            val errorMessage: String?
            try {
                errorCode = JSONObjectUtils.parse(e.message)["errorCode"]?.toString()
                errorMessage = JSONObjectUtils.parse(e.message)["message"]?.toString()
            } catch (parseException: ParseException) {
                log.error(parseException) { "Feil med parsing av koder fra SPK: ${parseException.message}" }
                    .also { Metrics.countTjenestepensjonSimuleringPre2025(UKJENT_FEIL_HOS_SPK) }
                throw RuntimeException("Feil med parsing av koder: ${parseException.message}")
            }

            log.warn(e) { "Feilmelding i respons fra SPK: $e" }
            if (spkMedlemskap == null || errorCode == null || errorMessage == null){
                Metrics.countTjenestepensjonSimuleringPre2025(UKJENT_FEIL_HOS_SPK)
                throw e
            }

            val feilkode = Feilkode.fromExternalValue(errorCode, errorMessage)
                .also { Metrics.countTjenestepensjonSimuleringPre2025(fromFeilkode(it)) }

            if (feilkode == Feilkode.BEREGNING_GIR_NULL_UTBETALING)
                return SimulerOffentligTjenestepensjonResult(
                    tpnr = spkMedlemskap.tpNr,
                    navnOrdning = spkMedlemskap.navn,
                    feilkode = feilkode,
                    utbetalingsperiodeListe = listOf(
                        Utbetalingsperiode(
                            datoFom = Alder.fromAlder(spec.foedselsdato, Alder(65, 0)),
                            datoTom = Alder.fromAlder(spec.foedselsdato, Alder(67, 0)),
                            uttaksgrad = 100,
                            arligUtbetaling = 0.0,
                            ytelsekode = YtelseCode.AFP
                        ),
                        Utbetalingsperiode(
                            datoFom = Alder.fromAlder(spec.foedselsdato, Alder(67, 0)),
                            datoTom = null,
                            uttaksgrad = 100,
                            arligUtbetaling = 0.0,
                            ytelsekode = YtelseCode.AP
                        )
                    )
                )

            return SimulerOffentligTjenestepensjonResult(
                tpnr = spkMedlemskap.tpNr,
                navnOrdning = spkMedlemskap.navn,
                feilkode = feilkode,
                utbetalingsperiodeListe = emptyList()
            )
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
            val MOCK_SPK_RESULT = SimulerOffentligTjenestepensjonResult(
                tpnr = "3010",
                navnOrdning = "Statens pensjonskasse",
                inkluderteOrdningerListe = listOf("Statens pensjonskasse"),
                leverandorUrl = "spk.no",
                utbetalingsperiodeListe = listOf(
                    Utbetalingsperiode(
                        uttaksgrad = 2,
                        arligUtbetaling = 6979.0,
                        datoFom = Alder.fromAlder(LocalDate.of(1961, 7, 25), Alder(64, 5)),
                        datoTom = Alder.fromAlder(LocalDate.of(1961, 7, 25), Alder(65, 0)),
                        ytelsekode = YtelseCode.AFP
                    ),
                    Utbetalingsperiode(
                        uttaksgrad = 2,
                        arligUtbetaling = 540000.0,
                        datoFom = Alder.fromAlder(LocalDate.of(1961, 7, 25), Alder(65, 0)),
                        datoTom = Alder.fromAlder(LocalDate.of(1961, 7, 25), Alder(67, 0)),
                        ytelsekode = YtelseCode.AFP
                    ),
                    Utbetalingsperiode(
                        uttaksgrad = 100,
                        arligUtbetaling = 173376.0,
                        datoFom = Alder.fromAlder(LocalDate.of(1961, 7, 25), Alder(67, 0)),
                        datoTom = Alder.fromAlder(LocalDate.of(1961, 7, 25), Alder(67, 5)),
                        ytelsekode = YtelseCode.AP
                    ),
                    Utbetalingsperiode(
                        uttaksgrad = 100,
                        arligUtbetaling = 167244.0,
                        datoFom = Alder.fromAlder(LocalDate.of(1961, 7, 25), Alder(67, 5)),
                        datoTom = Alder.fromAlder(LocalDate.of(1961, 7, 25), Alder(68, 5)),
                        ytelsekode = YtelseCode.AP
                    ),
                    Utbetalingsperiode(
                        uttaksgrad = 100,
                        arligUtbetaling = 163476.0,
                        datoFom = Alder.fromAlder(LocalDate.of(1961, 7, 25), Alder(68, 5)),
                        datoTom = null,
                        ytelsekode = YtelseCode.AP
                    )
                ),
                brukerErIkkeMedlemAvTPOrdning = false,
                brukerErMedlemAvTPOrdningSomIkkeStoettes = false,
            )
        }
    }