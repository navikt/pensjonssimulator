package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp

import mu.KotlinLogging
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Ordning
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.OffentligTjenestepensjonFra2025SimuleringSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.TjenestepensjonInntektSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.*
import java.time.LocalDate

object KlpMapper {

    private val log = KotlinLogging.logger {}

    fun toRequestDto(spec: OffentligTjenestepensjonFra2025SimuleringSpec) =
        KlpSimulerTjenestepensjonRequest(
            personId = spec.pid.value,
            uttaksListe = listOf(heltUttakAlleYtelser(fom = spec.uttaksdato)),
            fremtidigInntektsListe = inntekter(spec),
            arIUtlandetEtter16 = spec.utlandAntallAar,
            epsPensjon = spec.epsHarPensjon,
            eps2G = spec.epsHarInntektOver2G
        )

    fun fromResponseDto(
        response: KlpSimulerTjenestepensjonResponse,
        request: KlpSimulerTjenestepensjonRequest? = null
    ) =
        SimulertTjenestepensjon(
            tpLeverandoer = EgressService.KLP.description,
            ordningsListe = response.inkludertOrdningListe.map { Ordning(tpNummer = it.tpnr) },
            utbetalingsperioder = response.utbetalingsListe.map(::utbetalingsperiode),
            aarsakIngenUtbetaling = response.arsakIngenUtbetaling.map { "${it.statusBeskrivelse}: ${it.ytelseType}" },
            betingetTjenestepensjonErInkludert = response.utbetalingsListe.any(::erBetingetTjenestepensjon),
            erSisteOrdning = response.arsakIngenUtbetaling.none(::erIkkeSisteOrdning),
            serviceData = listOf("Request: ${request?.toString()}", "Response: $response")
        ).also {
            log.info { "Mapping response from KLP $response" }
        }

    private fun heltUttakAlleYtelser(fom: LocalDate) =
        Uttak(
            ytelseType = KlpTjenestepensjonYtelseType.ALLE.externalValue,
            fraOgMedDato = fom,
            uttaksgrad = 100 // prosent
        )

    private fun inntekter(spec: OffentligTjenestepensjonFra2025SimuleringSpec): MutableList<FremtidigInntekt> {
        val inntektListe = mutableListOf(naaverendeInntekt(aarligInntekt = spec.sisteInntekt))
        inntektListe.addAll(spec.fremtidigeInntekter.map(::inntekt))
        return inntektListe
    }

    private fun naaverendeInntekt(aarligInntekt: Int) =
        FremtidigInntekt(
            fraOgMedDato = LocalDate.now(),
            arligInntekt = aarligInntekt
        )

    private fun inntekt(source: TjenestepensjonInntektSpec) =
        FremtidigInntekt(
            fraOgMedDato = source.fom,
            arligInntekt = source.aarligInntekt
        )

    private fun utbetalingsperiode(source: Utbetaling) =
        Utbetalingsperiode(
            fom = source.fraOgMedDato,
            maanedligBelop = source.manedligUtbetaling,
            ytelseType = KlpTjenestepensjonYtelseType.internalValue(externalValue = source.ytelseType)?.kode ?: "ukjent"
        )

    private fun erBetingetTjenestepensjon(utbetaling: Utbetaling): Boolean =
        utbetaling.ytelseType == KlpTjenestepensjonYtelseType.BETINGET_TJENESTEPENSJON.externalValue

    private fun erIkkeSisteOrdning(aarsak: ArsakIngenUtbetaling): Boolean =
        aarsak.statusKode == AarsakIngenUtbetalingStatus.IKKE_SISTE_ORDNING.name
}