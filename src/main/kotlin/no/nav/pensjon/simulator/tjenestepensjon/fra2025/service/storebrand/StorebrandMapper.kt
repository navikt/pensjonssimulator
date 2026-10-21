package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand

import mu.KotlinLogging
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Ordning
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.OffentligTjenestepensjonFra2025SimuleringSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonInntektSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand.acl.AarsakIngenUtbetaling
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand.acl.AarsakIngenUtbetalingStatus
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand.acl.Delytelse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand.acl.FremtidigInntekt
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand.acl.StorebrandSimulerTjenestepensjonRequest
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand.acl.StorebrandSimulerTjenestepensjonResponse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand.acl.StorebrandTjenestepensjonYtelseType
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand.acl.Utbetaling
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand.acl.Uttak
import java.time.LocalDate

object StorebrandMapper {

    private val log = KotlinLogging.logger {}

    fun toRequestDto(spec: OffentligTjenestepensjonFra2025SimuleringSpec) =
        StorebrandSimulerTjenestepensjonRequest(
            personId = spec.pid.value,
            uttaksListe = listOf(uttak(spec)),
            fremtidigInntektListe = inntekter(spec),
            aarIUtlandetEtter16 = spec.utlandAntallAar,
            epsPensjon = spec.epsHarPensjon,
            eps2G = spec.epsHarInntektOver2G
        )

    fun fromResponseDto(
        response: StorebrandSimulerTjenestepensjonResponse,
        request: StorebrandSimulerTjenestepensjonRequest? = null
    ) =
        SimulertTjenestepensjon(
            tpLeverandoer = EgressService.STOREBRAND.description,
            ordningsListe = response.inkludertOrdningListe.map { Ordning(tpNummer = it.tpnr) },
            utbetalingsperioder = response.utbetalingListe.flatMap(::utbetalingsperioder),
            aarsakIngenUtbetaling = response.aarsakIngenUtbetaling?.map { "${it.statusBeskrivelse}: ${it.ytelseType}" } ?: emptyList(),
            betingetTjenestepensjonErInkludert = response.utbetalingListe
                .flatMap { it.delytelseListe }
                .any(::erBetingetTjenestepensjon),
            erSisteOrdning = response.aarsakIngenUtbetaling?.none(::erIkkeSisteOrdning) ?: true,
            serviceData = listOf("Request: ${request?.toString()}", "Response: $response")
        ).also {
            log.info { "Mapping response from STOREBRAND $response" }
        }

    /**
     * Inntekter f.o.m. fjorårets første dag til siste 'f.o.m.'-dato i listen over fremtidige inntekter.
     */
    private fun inntekter(spec: OffentligTjenestepensjonFra2025SimuleringSpec): List<FremtidigInntekt> {
        val inntektListe = mutableListOf(naaverendeInntekt(aarligInntekt = spec.sisteInntekt))
        inntektListe.addAll(spec.fremtidigeInntekter.map(::inntekt))
        return inntektListe
    }

    private fun naaverendeInntekt(aarligInntekt: Int) =
        FremtidigInntekt(
            fraOgMedDato = fjoraaretsFoersteDag(),
            aarligInntekt = aarligInntekt
        )

    private fun inntekt(spec: TjenestepensjonInntektSpec) =
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

    private fun uttak(spec: OffentligTjenestepensjonFra2025SimuleringSpec) =
        Uttak(
            ytelseType = StorebrandTjenestepensjonYtelseType.ALLE.externalValue,
            fraOgMedDato = spec.uttaksdato,
            uttaksgrad = 100
        )

    private fun erBetingetTjenestepensjon(ytelse: Delytelse): Boolean =
        ytelse.ytelseType == StorebrandTjenestepensjonYtelseType.BETINGET_TJENESTEPENSJON.externalValue

    private fun erIkkeSisteOrdning(aarsak: AarsakIngenUtbetaling): Boolean =
        aarsak.statusKode == AarsakIngenUtbetalingStatus.IKKE_SISTE_ORDNING.name

    private fun fjoraaretsFoersteDag(): LocalDate =
        LocalDate.now().minusYears(1).withDayOfYear(1)
}