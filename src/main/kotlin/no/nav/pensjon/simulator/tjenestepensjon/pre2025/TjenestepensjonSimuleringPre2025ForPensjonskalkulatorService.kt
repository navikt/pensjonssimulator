package no.nav.pensjon.simulator.tjenestepensjon.pre2025

import mu.KotlinLogging
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.tech.env.EnvironmentUtil
import no.nav.pensjon.simulator.tech.metric.Metrics
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService
import no.nav.pensjon.simulator.tech.toggle.FeatureToggleService.Companion.PEK_1490_TP_FOER_1963
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.Tjenestepensjonsforhold
import no.nav.pensjon.simulator.tjenestepensjon.Tjenestepensjonsordning
import no.nav.pensjon.simulator.tjenestepensjon.TjenestepensjonException
import no.nav.pensjon.simulator.tjenestepensjon.forhold.client.TjenestepensjonsforholdClient
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.SimulerOffentligTjenestepensjonResult.Companion.ikkeMedlem
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.SimulerOffentligTjenestepensjonResult.Companion.tpOrdningStoettesIkke
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.Pre2025TjenestepensjonService
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.TjenestepensjonSimuleringPre2025Spec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.client.spk.metrics.TellbarSpkResultatkode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.stillingsprosent.StillingsprosentService
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Service for V3 in TjenestepensjonPre2025Controller.
 */
@Component
class TjenestepensjonSimuleringPre2025ForPensjonskalkulatorService(
    private val forholdClient: TjenestepensjonsforholdClient,
    private val stillingsprosentService: StillingsprosentService,
    private val simuleringService: Pre2025TjenestepensjonService,
    private val featureToggleService: FeatureToggleService
) {
    private val log = KotlinLogging.logger { }

    fun simuler(spec: TjenestepensjonSimuleringPre2025Spec): SimulerOffentligTjenestepensjonResult {
        var tpOrdning: Tjenestepensjonsordning? = null

        try {
            val pid = spec.pid

            val alleForhold: List<Tjenestepensjonsordning> =
                forholdClient.findAlleTpForhold(pid).mapNotNull(::tpOrdning)

            if (alleForhold.isEmpty()) {
                return ikkeMedlem()
            }

            tpOrdning = alleForhold.firstOrNull { relevanteTpNumre.contains(it.tpNr) }

            if (tpOrdning == null) {
                log.warn { "No supported TP-Ordning found" }
                return tpOrdningStoettesIkke(relevanteTpOrdninger = alleForhold.map { it.navn })
            }

            if (EnvironmentUtil.isDevelopment() && featureToggleService.isEnabled(PEK_1490_TP_FOER_1963)) {
                if (spec.pid.value == "19456138058") throw MOCK_SPK_EXCEPTION1
                if (spec.pid.value == "13426143482") throw MOCK_SPK_EXCEPTION2
                if (spec.pid.value == "26456120872") throw MOCK_SPK_EXCEPTION3
                if (spec.pid.value == "25476113736") return MOCK_SPK_RESULT
            }

            val stillingsprosentListe = stillingsprosentService.getStillingsprosentListe(pid, tpOrdning = tpOrdning)

            if (stillingsprosentListe.isEmpty()) {
                log.warn { "No stillingsprosent found" }
                    .also { Metrics.countTjenestepensjonSimuleringPre2025(TellbarSpkResultatkode.INGEN_STILLINGSPROSENT) }

                return SimulerOffentligTjenestepensjonResult(
                    tpnr = tpOrdning.tpNr,
                    navnOrdning = tpOrdning.navn,
                    inkluderteOrdningerListe = emptyList(),
                    feilkode = Feilkode.TEKNISK_FEIL,
                    relevanteTpOrdninger = alleForhold.map { it.navn }
                )
            }

            return simuleringService.simulerOffentligTjenestepensjon(spec, stillingsprosentListe, tpOrdning)
        } catch (e: TjenestepensjonException) {
            log.warn(e) { "Feilmelding i respons fra $TP_ORDNING_NAVN: ${e.message}" }

            return SimulerOffentligTjenestepensjonResult(
                tpnr = tpOrdning!!.tpNr,
                navnOrdning = tpOrdning.navn,
                utbetalingsperiodeListe = e.resultat.utbetalingsperiodeListe,
                feilkode = e.resultat.feilkode
            )
        }
    }

    private fun tpOrdning(forhold: Tjenestepensjonsforhold): Tjenestepensjonsordning? =
        forholdClient.findTssId(tpNr = forhold.tpNr)?.let {
            Tjenestepensjonsordning(
                navn = forhold.navn,
                tpNr = forhold.tpNr,
                datoSistOpptjening = forhold.datoSistOpptjening,
                tssId = it
            )
        }

    private companion object {

        private const val TP_ORDNING_NAVN = "SPK"
        private val relevanteTpNumre = setOf("3010", "3060")

        private val MOCK_SPK_EXCEPTION1 = EgressException(
            """{"errorCode":"CALC002","message":"Validation problem: Tjenestetid mindre enn 3 år."}""",
        )

        private val MOCK_SPK_EXCEPTION2 = EgressException(
            """{"errorCode":"CALC002","message":"Validation problem: Flere samtidige stillinger er ikke støttet."}""",
        )

        private val MOCK_SPK_EXCEPTION3 = EgressException(
            """{"errorCode":"CALC002","message":"Validation problem: Beregning gir 0 i utbetaling."}""",
        )

        private val MOCK_SPK_RESULT = SimulerOffentligTjenestepensjonResult(
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
            brukerErMedlemAvTPOrdningSomIkkeStoettes = false
        )
    }
}