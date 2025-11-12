package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk

import mu.KotlinLogging
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tjenestepensjon.TjenestepensjonYtelseType
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Ordning
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.OffentligTjenestepensjonFra2025SimuleringSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.FremtidigInntekt
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.SpkSimulerTjenestepensjonRequest
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.SpkSimulerTjenestepensjonResponse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.SpkYtelse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.Uttak
import java.time.LocalDate

object SpkMapper {
    private val log = KotlinLogging.logger {}

    fun toRequestDto(spec: OffentligTjenestepensjonFra2025SimuleringSpec): SpkSimulerTjenestepensjonRequest =
        spec.fremtidigeInntekter
            ?.let { mapToRequestV2(spec) }
            ?: mapToRequestV1(spec)

    private fun mapToRequestV1(spec: OffentligTjenestepensjonFra2025SimuleringSpec) =
        SpkSimulerTjenestepensjonRequest(
            personId = spec.pid.value,
            uttaksListe = opprettUttaksliste(spec),
            fremtidigInntektListe = listOf(
                opprettNaaverendeInntektFoerUttak(spec),
                FremtidigInntekt(fraOgMedDato = spec.uttaksdato, aarligInntekt = 0)
            ),
            aarIUtlandetEtter16 = spec.utlandAntallAar,
            epsPensjon = spec.epsHarPensjon,
            eps2G = spec.epsHarInntektOver2G,
        )

    private fun mapToRequestV2(spec: OffentligTjenestepensjonFra2025SimuleringSpec): SpkSimulerTjenestepensjonRequest {
        val fremtidigeInntekter: MutableList<FremtidigInntekt> =
            mutableListOf(opprettNaaverendeInntektFoerUttak(spec))

        fremtidigeInntekter.addAll(spec.fremtidigeInntekter.orEmpty().map {
            FremtidigInntekt(fraOgMedDato = it.fom, aarligInntekt = it.aarligInntekt)
        })

        return SpkSimulerTjenestepensjonRequest(
            personId = spec.pid.value,
            uttaksListe = opprettUttaksliste(spec),
            fremtidigInntektListe = fremtidigeInntekter,
            aarIUtlandetEtter16 = spec.utlandAntallAar,
            epsPensjon = spec.epsHarPensjon,
            eps2G = spec.epsHarInntektOver2G,
        )
    }

    fun fromResponseDto(
        response: SpkSimulerTjenestepensjonResponse,
        request: SpkSimulerTjenestepensjonRequest? = null
    ): SimulertTjenestepensjon {
        log.info { "Mapping response from SPK $response" }
        return SimulertTjenestepensjon(
            tpLeverandoer = EgressService.SPK.description,
            ordningsListe = response.inkludertOrdningListe.map { Ordning(tpNummer = it.tpnr) },
            utbetalingsperioder = response.utbetalingListe.flatMap { periode ->
                val fraOgMed = periode.fraOgMedDato
                periode.delytelseListe.map { Utbetalingsperiode(fraOgMed, it.maanedligBelop, it.ytelseType) }
            },
            aarsakIngenUtbetaling = response.aarsakIngenUtbetaling.map { it.statusBeskrivelse + ": " + it.ytelseType },
            betingetTjenestepensjonErInkludert = response.utbetalingListe.flatMap { it.delytelseListe }
                .any { it.ytelseType == TjenestepensjonYtelseType.BETINGET_TJENESTEPENSJON.kode },
            erSisteOrdning = response.aarsakIngenUtbetaling.none { it.statusKode == "IKKE_SISTE_ORDNING" }, //TODO enum
            serviceData = listOf("Request: ${request?.toString()}", "Response: $response")
        )
    }

    private fun opprettUttaksliste(request: OffentligTjenestepensjonFra2025SimuleringSpec): List<Uttak> =
        SpkYtelse.hentAlleUnntattType(if (request.afpErForespurt) SpkYtelse.BTP else SpkYtelse.OAFP)
            .map {
                Uttak(
                    ytelseType = it,
                    fraOgMedDato = request.uttaksdato,
                    uttaksgrad = null
                )
            }

    private fun opprettNaaverendeInntektFoerUttak(spec: OffentligTjenestepensjonFra2025SimuleringSpec) =
        FremtidigInntekt(
            fraOgMedDato = fjorAarSomManglerOpptjeningIPopp(),
            aarligInntekt = spec.sisteInntekt
        )

    private fun fjorAarSomManglerOpptjeningIPopp(): LocalDate =
        LocalDate.now().minusYears(1).withDayOfYear(1)
}
