package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp

import mu.KotlinLogging
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tjenestepensjon.TjenestepensjonYtelseType
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Ordning
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.OffentligTjenestepensjonFra2025SimuleringSpec
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.FremtidigInntekt
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.KlpSimulerTjenestepensjonRequest
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.KlpSimulerTjenestepensjonResponse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.Uttak
import java.time.LocalDate

object KlpMapper {

    private const val ANNEN_TP_ORDNING_BURDE_SIMULERE = "IKKE_SISTE_ORDNING"
    private val log = KotlinLogging.logger {}

    fun toRequestDto(spec: OffentligTjenestepensjonFra2025SimuleringSpec): KlpSimulerTjenestepensjonRequest {
        val fremtidigInntektsListe = mutableListOf(opprettNaaverendeInntektFoerUttak(spec))

        fremtidigInntektsListe.addAll(
            spec.fremtidigeInntekter.orEmpty().map {
                FremtidigInntekt(fraOgMedDato = it.fom, arligInntekt = it.aarligInntekt)
            }
        )

        return KlpSimulerTjenestepensjonRequest(
            personId = spec.pid.value,
            uttaksListe = listOf(
                Uttak(
                    ytelseType = TjenestepensjonYtelseType.ALLE.kode,
                    fraOgMedDato = spec.uttaksdato,
                    uttaksgrad = 100
                )
            ),
            fremtidigInntektsListe = fremtidigInntektsListe,
            arIUtlandetEtter16 = spec.utlandAntallAar,
            epsPensjon = spec.epsHarPensjon,
            eps2G = spec.epsHarInntektOver2G,
        )
    }

    fun fromResponseDto(
        response: KlpSimulerTjenestepensjonResponse,
        request: KlpSimulerTjenestepensjonRequest? = null
    ): SimulertTjenestepensjon {
        log.info { "Mapping response from KLP $response" }
        return SimulertTjenestepensjon(
            tpLeverandoer = EgressService.KLP.description,
            ordningsListe = response.inkludertOrdningListe.map { Ordning(tpNummer = it.tpnr) },
            utbetalingsperioder = response.utbetalingsListe.map {
                Utbetalingsperiode(it.fraOgMedDato, it.manedligUtbetaling, it.ytelseType)
            },
            aarsakIngenUtbetaling = response.arsakIngenUtbetaling.map { "${it.statusBeskrivelse}: ${it.ytelseType}" },
            betingetTjenestepensjonErInkludert = response.utbetalingsListe.any {
                it.ytelseType == TjenestepensjonYtelseType.BETINGET_TJENESTEPENSJON.kode
            },
            erSisteOrdning = response.arsakIngenUtbetaling.none { it.statusKode == ANNEN_TP_ORDNING_BURDE_SIMULERE },
            serviceData = listOf("Request: ${request?.toString()}", "Response: $response")
        )
    }

    private fun opprettNaaverendeInntektFoerUttak(spec: OffentligTjenestepensjonFra2025SimuleringSpec) =
        FremtidigInntekt(
            fraOgMedDato = LocalDate.now(),
            arligInntekt = spec.sisteInntekt
        )
}