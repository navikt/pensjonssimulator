package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk

import mu.KotlinLogging
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Ordning
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.OffentligTjenestepensjonFra2025SimuleringSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonInntektSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.Delytelse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.FremtidigInntekt
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.SpkSimulerTjenestepensjonRequest
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.SpkSimulerTjenestepensjonResponse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.SpkTjenestepensjonYtelseType
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.Utbetaling
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.Uttak
import java.time.LocalDate

object SpkMapper {
    private val log = KotlinLogging.logger {}

    fun toRequestDto(spec: OffentligTjenestepensjonFra2025SimuleringSpec) =
        SpkSimulerTjenestepensjonRequest(
            personId = spec.pid.value,
            uttaksListe = uttakListe(spec),
            fremtidigInntektListe = inntekter(spec),
            aarIUtlandetEtter16 = spec.utlandAntallAar,
            epsPensjon = spec.epsHarPensjon,
            eps2G = spec.epsHarInntektOver2G
        )

    fun fromResponseDto(
        response: SpkSimulerTjenestepensjonResponse,
        request: SpkSimulerTjenestepensjonRequest? = null
    ): SimulertTjenestepensjon {
        log.info { "Mapping response from SPK $response" }
        return SimulertTjenestepensjon(
            tpLeverandoer = EgressService.SPK.description,
            ordningsListe = response.inkludertOrdningListe.map { Ordning(tpNummer = it.tpnr) },
            utbetalingsperioder = response.utbetalingListe.flatMap(::utbetalingsperioder),
            aarsakIngenUtbetaling = response.aarsakIngenUtbetaling.map { "${it.statusBeskrivelse}: ${it.ytelseType}" },
            betingetTjenestepensjonErInkludert = response.utbetalingListe
                .flatMap { it.delytelseListe }
                .any(::erBetingetTjenestepensjon),
            erSisteOrdning = response.aarsakIngenUtbetaling.none { it.statusKode == "IKKE_SISTE_ORDNING" }, //TODO enum
            serviceData = listOf("Request: ${request?.toString()}", "Response: $response")
        )
    }

    /**
     * Inntekter f.o.m. fjorårets første dag til siste 'f.o.m.'-dato i listen over fremtidige inntekter.
     */
    private fun inntekter(spec: OffentligTjenestepensjonFra2025SimuleringSpec): List<FremtidigInntekt> {
        val fremtidigeInntekter: MutableList<FremtidigInntekt> =
            mutableListOf(naaverendeInntekt(aarligInntekt = spec.sisteInntekt))

        fremtidigeInntekter.addAll(spec.fremtidigeInntekter.map(::inntekt))
        return fremtidigeInntekter
    }

    private fun naaverendeInntekt(aarligInntekt: Int) =
        FremtidigInntekt(
            fraOgMedDato = fjoraaretsFoersteDag(),
            aarligInntekt = aarligInntekt
        )

    private fun inntekt(spec: TjenestepensjonInntektSpec): FremtidigInntekt =
        FremtidigInntekt(
            fraOgMedDato = spec.fom,
            aarligInntekt = spec.aarligInntekt
        )

    private fun utbetalingsperioder(utbetaling: Utbetaling): List<Utbetalingsperiode> =
        utbetaling.delytelseListe.map {
            Utbetalingsperiode(
                fom = utbetaling.fraOgMedDato,
                maanedligBelop = it.maanedligBelop,
                ytelseType = it.ytelseType
            )
        }

    private fun uttakListe(spec: OffentligTjenestepensjonFra2025SimuleringSpec): List<Uttak> =
        SpkTjenestepensjonYtelseType.alleUnntatt(ekskludertType(spec.afpErForespurt))
            .map {
                Uttak(
                    ytelseType = it.externalValue,
                    fraOgMedDato = spec.uttaksdato,
                    uttaksgrad = null
                )
            }

    private fun ekskludertType(afpErForespurt: Boolean): SpkTjenestepensjonYtelseType =
        if (afpErForespurt)
            SpkTjenestepensjonYtelseType.BETINGET_TJENESTEPENSJON
        else
            SpkTjenestepensjonYtelseType.OFFENTLIG_AFP

    private fun erBetingetTjenestepensjon(ytelse: Delytelse): Boolean =
        ytelse.ytelseType == SpkTjenestepensjonYtelseType.BETINGET_TJENESTEPENSJON.externalValue

    private fun fjoraaretsFoersteDag(): LocalDate =
        LocalDate.now().minusYears(1).withDayOfYear(1)
}
