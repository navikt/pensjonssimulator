package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk

import mu.KotlinLogging
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Ordning
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerOffentligTjenestepensjonFra2025SpecV1
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.FremtidigInntekt
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.SPKSimulerTjenestepensjonRequest
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.SPKSimulerTjenestepensjonResponse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.SPKYtelse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.Uttak
import java.time.LocalDate

object SPKMapper {
    private val log = KotlinLogging.logger {}

    const val PROVIDER_FULLT_NAVN = "Statens pensjonskasse"

    fun mapToRequest(request: SimulerOffentligTjenestepensjonFra2025SpecV1): SPKSimulerTjenestepensjonRequest {
        return request.fremtidigeInntekter
            ?.let { mapToRequestV2(request) }
            ?: mapToRequestV1(request)
    }

    private fun mapToRequestV1(request: SimulerOffentligTjenestepensjonFra2025SpecV1) = SPKSimulerTjenestepensjonRequest(
        personId = request.pid,
        uttaksListe = opprettUttaksliste(request),
        fremtidigInntektListe = listOf(
            opprettNaaverendeInntektFoerUttak(request),
            FremtidigInntekt(
                fraOgMedDato = request.uttaksdato,
                aarligInntekt = 0
            )
        ),
        aarIUtlandetEtter16 = request.aarIUtlandetEtter16,
        epsPensjon = request.epsPensjon,
        eps2G = request.eps2G,
    )

    private fun mapToRequestV2(request: SimulerOffentligTjenestepensjonFra2025SpecV1): SPKSimulerTjenestepensjonRequest {
        val fremtidigeInntekter: MutableList<FremtidigInntekt> = mutableListOf(opprettNaaverendeInntektFoerUttak(request))
        fremtidigeInntekter.addAll(request.fremtidigeInntekter?.map {
            FremtidigInntekt(
                fraOgMedDato = it.fraOgMed,
                aarligInntekt = it.aarligInntekt
            )
        } ?: emptyList())
        return SPKSimulerTjenestepensjonRequest(
            personId = request.pid,
            uttaksListe = opprettUttaksliste(request),
            fremtidigInntektListe = fremtidigeInntekter,
            aarIUtlandetEtter16 = request.aarIUtlandetEtter16,
            epsPensjon = request.epsPensjon,
            eps2G = request.eps2G,
        )
    }

    private fun opprettNaaverendeInntektFoerUttak(request: SimulerOffentligTjenestepensjonFra2025SpecV1) = FremtidigInntekt(
        fraOgMedDato = fjorAarSomManglerOpptjeningIPopp(),
        aarligInntekt = request.sisteInntekt
    )

    private fun fjorAarSomManglerOpptjeningIPopp(): LocalDate = LocalDate.now().minusYears(1).withDayOfYear(1)

    fun mapToResponse(response: SPKSimulerTjenestepensjonResponse, dto: SPKSimulerTjenestepensjonRequest? = null): SimulertTjenestepensjon {
        log.info { "Mapping response from SPK $response" }
        return SimulertTjenestepensjon(
            tpLeverandoer = PROVIDER_FULLT_NAVN,
            ordningsListe = response.inkludertOrdningListe.map { Ordning(it.tpnr) },
            utbetalingsperioder = response.utbetalingListe.flatMap { periode ->
                val fraOgMed = periode.fraOgMedDato
                periode.delytelseListe.map { Utbetalingsperiode(fraOgMed, it.maanedligBelop, it.ytelseType) }
            },
            aarsakIngenUtbetaling = response.aarsakIngenUtbetaling.map { it.statusBeskrivelse + ": " + it.ytelseType },
            betingetTjenestepensjonErInkludert = response.utbetalingListe.flatMap { it.delytelseListe }.any { it.ytelseType == "BTP" },
            erSisteOrdning = response.aarsakIngenUtbetaling.none { it.statusKode == "IKKE_SISTE_ORDNING" },
            serviceData = listOf("Request: " + dto?.toString(), "Response: $response")
        )
    }

    fun opprettUttaksliste(request: SimulerOffentligTjenestepensjonFra2025SpecV1): List<Uttak> {
        return SPKYtelse.hentAlleUnntattType(if (request.brukerBaOmAfp) SPKYtelse.BTP else SPKYtelse.OAFP)
            .map {
                Uttak(
                    ytelseType = it,
                    fraOgMedDato = request.uttaksdato,
                    uttaksgrad = null
                )
            }
    }
}