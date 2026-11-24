package no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.client.spk

import com.nimbusds.jose.util.JSONObjectUtils
import mu.KotlinLogging
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.tech.metric.Metrics.countTjenestepensjonSimuleringPre2025
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.tjenestepensjon.TjenestepensjonException
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.Feilkode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.TjenestepensjonSimuleringsresultatVedFeil
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.YtelseCode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.client.spk.acl.SpkFeilkode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.client.spk.metrics.TellbarSpkResultatkode
import org.apache.el.parser.ParseException
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class SpkFeilhaandtering {

    private val log = KotlinLogging.logger { }

    fun haandterFeil(e: EgressException, foedselsdato: LocalDate): Nothing {
        log.warn(e) { "Feilmelding i respons fra SPK: ${e.message}" }

        throw TjenestepensjonException(
            message = "prognosekall til SPK feilet",
            resultat = resultatVedFeil(feilkode(e), foedselsdato)
        )
    }

    private fun feilkode(e: EgressException): Feilkode =
        try {
            val externalErrorCode = extractValue(e, field = "errorCode") ?: ukjentFeil(e)
            val externalErrorMessage = extractValue(e, field = "message") ?: ukjentFeil(e)

            SpkFeilkode.internalValue(externalErrorCode, externalErrorMessage)
                .also { count(TellbarSpkResultatkode.fromFeilkode(it)) }
        } catch (e: ParseException) {
            log.error(e) { "Feil med parsing av koder fra SPK: ${e.message}" }
                .also { count(TellbarSpkResultatkode.UKJENT_FEIL_HOS_SPK) }
            throw RuntimeException("Feil med parsing av koder: ${e.message}")
        }

    companion object {

        private fun count(resultat: TellbarSpkResultatkode) {
            countTjenestepensjonSimuleringPre2025(resultat)
        }

        private fun resultatVedFeil(feilkode: Feilkode, foedselsdato: LocalDate) =
            TjenestepensjonSimuleringsresultatVedFeil(
                utbetalingsperiodeListe(feilkode, foedselsdato),
                feilkode
            )

        private fun utbetalingsperiodeListe(
            feilkode: Feilkode,
            foedselsdato: LocalDate
        ): List<Utbetalingsperiode> =
            if (feilkode == Feilkode.BEREGNING_GIR_NULL_UTBETALING)
                utbetalingsperioderVedNullUtbetaling(foedselsdato)
            else
                emptyList()

        private fun utbetalingsperioderVedNullUtbetaling(foedselsdato: LocalDate): List<Utbetalingsperiode> =
            listOf(
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

        private fun ukjentFeil(e: EgressException): Nothing {
            count(TellbarSpkResultatkode.UKJENT_FEIL_HOS_SPK)
            throw e
        }

        private fun extractValue(e: EgressException, field: String): String? =
            JSONObjectUtils.parse(e.message)[field]?.toString()

        private fun datoVedAlder(alderAar: Int, foedselsdato: LocalDate): LocalDate =
            Alder.fromAlder(foedselsdato, Alder(alderAar, maaneder = 0))
    }
}