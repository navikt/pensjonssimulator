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
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.metrics.SPKResultatKodePre2025
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.metrics.SPKResultatKodePre2025.*
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.SPKTjenestepensjonServicePre2025
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.TjenestepensjonSimuleringPre2025Spec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.SpkStillingsprosentService
import no.nav.pensjon.simulator.tpregisteret.TpOrdning
import no.nav.pensjon.simulator.tpregisteret.TpOrdningId
import no.nav.pensjon.simulator.tpregisteret.TpregisteretClient
import org.apache.el.parser.ParseException
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Service for V3 in TjenestepensjonPre2025Controller.
 */
@Component
class TjenestepensjonSimuleringPre2025ForPensjonskalkulatorService(
    private val tpregisteretClient: TpregisteretClient,
    private val spkStillingsprosentService: SpkStillingsprosentService,
    private val spkTjenestepensjonServicePre2025: SPKTjenestepensjonServicePre2025,
    private val featureToggleService: FeatureToggleService
) {
    val log = KotlinLogging.logger { }

    fun simuler(spec: TjenestepensjonSimuleringPre2025Spec): SimulerOffentligTjenestepensjonResult {
        log.debug { "Simulering av tjenestepensjon pre 2025: ${filterFnr(spec.toString())}" }
        var spkMedlemskap: TpOrdning? = null

        try {
            val pid = spec.pid

            val alleForhold: List<TpOrdning> = tpregisteretClient.findAlleTpForhold(pid)
                .mapNotNull { forhold ->
                    tpregisteretClient.findTssId(forhold.tpNr)
                        ?.let { TpOrdningId(tpId = forhold.tpNr, tssId = it) }
                        ?.toTpOrdning(forhold)
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


                if (spec.pid.value == "19456138058") throw MOCK_SPK_EXCEPTION1
                if (spec.pid.value == "13426143482") throw MOCK_SPK_EXCEPTION2
                if (spec.pid.value == "26456120872") throw MOCK_SPK_EXCEPTION3
                if (spec.pid.value == "25476113736") return MOCK_SPK_RESULT

            val stillingsprosentListe = spkStillingsprosentService.getStillingsprosentListe(pid, tpOrdning = spkMedlemskap)

            if (stillingsprosentListe.isEmpty()) {
                log.warn { "No stillingsprosent found" }
                    .also { Metrics.countTjenestepensjonSimuleringPre2025(INGEN_STILLINGSPROSENT) }

                return SimulerOffentligTjenestepensjonResult(
                    tpnr = spkMedlemskap.tpNr,
                    navnOrdning = spkMedlemskap.navn,
                    inkluderteOrdningerListe = emptyList(),
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
            log.warn(e) { "Feilmelding i respons fra SPK: $e" }

            return spkMedlemskap?.let {
                resultatVedFeil(
                    feilkode = feilkode(exception = e),
                    tpOrdning = it,
                    foedselsdato = spec.foedselsdato
                )
            } ?: ukjentFeil(exception = e)
        }
    }

    private fun feilkode(exception: EgressException): Feilkode =
        try {
            val externalErorCode = extractValue(exception, field = "errorCode") ?: ukjentFeil(exception)
            val externalErrorMessage = extractValue(exception, field = "message") ?: ukjentFeil(exception)

            Feilkode.fromExternalValue(externalErorCode, externalErrorMessage)
                .also { count(SPKResultatKodePre2025.fromFeilkode(it)) }
        } catch (e: ParseException) {
            log.error(e) { "Feil med parsing av koder fra SPK: ${e.message}" }
                .also { count(UKJENT_FEIL_HOS_SPK) }
            throw RuntimeException("Feil med parsing av koder: ${e.message}")
        }

    private companion object {
        private val FNR_REGEX = """[0-9]{11}""".toRegex()

        private fun resultatVedFeil(
            feilkode: Feilkode,
            tpOrdning: TpOrdning,
            foedselsdato: LocalDate
        ): SimulerOffentligTjenestepensjonResult =
            if (feilkode == Feilkode.BEREGNING_GIR_NULL_UTBETALING)
                resultatVedFeil(
                    feilkode,
                    tpOrdning,
                    utbetalingsperiodeListe = listOf(
                        Utbetalingsperiode(
                            datoFom = datoVedAlder(alderAar = 65, foedselsdato),
                            datoTom = datoVedAlder(alderAar = 67, foedselsdato),
                            uttaksgrad = 100,
                            arligUtbetaling = 0.0,
                            ytelsekode = YtelseCode.AFP
                        ),
                        Utbetalingsperiode(
                            datoFom = datoVedAlder(alderAar = 67, foedselsdato),
                            datoTom = null,
                            uttaksgrad = 100,
                            arligUtbetaling = 0.0,
                            ytelsekode = YtelseCode.AP
                        )
                    )
                )
            else
                resultatVedFeil(
                    feilkode,
                    tpOrdning,
                    utbetalingsperiodeListe = emptyList()
                )

        private fun resultatVedFeil(
            feilkode: Feilkode,
            tpOrdning: TpOrdning,
            utbetalingsperiodeListe: List<Utbetalingsperiode>
        ) =
            SimulerOffentligTjenestepensjonResult(
                tpnr = tpOrdning.tpNr,
                navnOrdning = tpOrdning.navn,
                feilkode = feilkode,
                utbetalingsperiodeListe = utbetalingsperiodeListe
            )

        private fun datoVedAlder(alderAar: Int, foedselsdato: LocalDate): LocalDate =
            Alder.fromAlder(foedselsdato, Alder(alderAar, maaneder = 0))

        private fun extractValue(exception: EgressException, field: String): String? =
            JSONObjectUtils.parse(exception.message)[field]?.toString()

        private fun count(resultatKode: SPKResultatKodePre2025) {
            Metrics.countTjenestepensjonSimuleringPre2025(resultatKode)
        }

        private fun ukjentFeil(exception: EgressException): Nothing {
            count(UKJENT_FEIL_HOS_SPK)
            throw exception
        }

        private fun filterFnr(s: String) = FNR_REGEX.replace(s, "*****")

            val MOCK_SPK_EXCEPTION1 = EgressException(
                """{"errorCode":"CALC002","message":"Validation problem: Tjenestetid mindre enn 3 år."}""",
            )
            val MOCK_SPK_EXCEPTION2 = EgressException(
                """{"errorCode":"CALC002","message":"Validation problem: Flere samtidige stillinger er ikke støttet."}""",
            )
            val MOCK_SPK_EXCEPTION3 = EgressException(
                """{"errorCode":"CALC002","message":"Validation problem: Beregning gir 0 i utbetaling."}""",
            )

            // ===== MOCK DATA CONFIG - Change birthdate here to update all dates =====
            private val MOCK_FOEDSELSDATO = LocalDate.of(1962, 7, 3)

            /** First day of month when person reaches given age (years, months) */
            private fun fom(aar: Int, maaneder: Int): LocalDate =
                Alder.fromAlder(MOCK_FOEDSELSDATO, Alder(aar, maaneder))

            /** Last day of month when person reaches given age (years, months) */
            private fun tom(aar: Int, maaneder: Int): LocalDate =
                fom(aar, maaneder).plusMonths(1).minusDays(1)

            val MOCK_SPK_RESULT = SimulerOffentligTjenestepensjonResult(
                tpnr = "3010",
                navnOrdning = "Statens pensjonskasse",
                inkluderteOrdningerListe = listOf("Statens pensjonskasse"),
                leverandorUrl = "spk.no",
                utbetalingsperiodeListe = listOf(
                    // AFP: 63 år 0 mnd -> 64 år 11 mnd
                    Utbetalingsperiode(
                        uttaksgrad = 2,
                        arligUtbetaling = 6979.0,
                        datoFom = fom(63, 0),
                        datoTom = tom(64, 11),
                        ytelsekode = YtelseCode.AFP
                    ),
                    // AFP: 65 år 0 mnd -> 66 år 11 mnd
                    Utbetalingsperiode(
                        uttaksgrad = 2,
                        arligUtbetaling = 540000.0,
                        datoFom = fom(65, 0),
                        datoTom = tom(66, 11),
                        ytelsekode = YtelseCode.AFP
                    ),
                    // AP: 67 år 0 mnd -> 67 år 4 mnd
                    Utbetalingsperiode(
                        uttaksgrad = 100,
                        arligUtbetaling = 173376.0,
                        datoFom = fom(67, 0),
                        datoTom = tom(67, 4),
                        ytelsekode = YtelseCode.AP
                    ),
                    // AP: 67 år 5 mnd -> 68 år 4 mnd
                    Utbetalingsperiode(
                        uttaksgrad = 100,
                        arligUtbetaling = 167244.0,
                        datoFom = fom(67, 5),
                        datoTom = tom(68, 4),
                        ytelsekode = YtelseCode.AP
                    ),
                    // AP: 68 år 5 mnd -> lifetime (null)
                    Utbetalingsperiode(
                        uttaksgrad = 100,
                        arligUtbetaling = 163476.0,
                        datoFom = fom(68, 5),
                        datoTom = null,
                        ytelsekode = YtelseCode.AP
                    )
                ),
                brukerErIkkeMedlemAvTPOrdning = false,
                brukerErMedlemAvTPOrdningSomIkkeStoettes = false
            )
    }
}