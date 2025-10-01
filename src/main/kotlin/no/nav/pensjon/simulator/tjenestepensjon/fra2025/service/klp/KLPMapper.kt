package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp

import mu.KotlinLogging
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Ordning
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerTjenestepensjonRequestDto
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.FremtidigInntekt
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.KLPSimulerTjenestepensjonRequest
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.KLPSimulerTjenestepensjonResponse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.KLPYtelse
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.Uttak
import java.time.LocalDate

object KLPMapper {

    private val log = KotlinLogging.logger {}
    const val PROVIDER_FULLT_NAVN = "Kommunal Landspensjonskasse"
    const val ANNEN_TP_ORDNING_BURDE_SIMULERE = "IKKE_SISTE_ORDNING"

    fun mapToRequest(request: SimulerTjenestepensjonRequestDto): KLPSimulerTjenestepensjonRequest {
        val fremtidigInntektsListe = mutableListOf(
            opprettNaaverendeInntektFoerUttak(request)
        )
        fremtidigInntektsListe.addAll(
            request.fremtidigeInntekter?.map {
                FremtidigInntekt(
                    fraOgMedDato = it.fraOgMed,
                    arligInntekt = it.aarligInntekt
                )
            } ?: emptyList()
        )

        return KLPSimulerTjenestepensjonRequest(
            personId = request.pid,
            uttaksListe = listOf(
                Uttak(
                    ytelseType = KLPYtelse.ALLE.name,
                    fraOgMedDato = request.uttaksdato,
                    uttaksgrad = 100
                )
            ),
            fremtidigInntektsListe = fremtidigInntektsListe,
            arIUtlandetEtter16 = request.aarIUtlandetEtter16,
            epsPensjon = request.epsPensjon,
            eps2G = request.eps2G,
        )
    }

    private fun opprettNaaverendeInntektFoerUttak(request: SimulerTjenestepensjonRequestDto) = FremtidigInntekt(
        fraOgMedDato = LocalDate.now(),
        arligInntekt = request.sisteInntekt
    )

    private fun aarUtenRegistrertInntektHosSkatteetaten(): LocalDate = LocalDate.now().minusYears(2).withDayOfYear(1)

    fun mapToResponse(response: KLPSimulerTjenestepensjonResponse, dto: KLPSimulerTjenestepensjonRequest? = null): SimulertTjenestepensjon {
        log.info { "Mapping response from KLP $response" }
        return SimulertTjenestepensjon(
            tpLeverandoer = PROVIDER_FULLT_NAVN,
            ordningsListe = response.inkludertOrdningListe.map { Ordning(it.tpnr) },
            utbetalingsperioder = response.utbetalingsListe.map { Utbetalingsperiode(it.fraOgMedDato, it.manedligUtbetaling, it.ytelseType) },
            aarsakIngenUtbetaling = response.arsakIngenUtbetaling.map { it.statusBeskrivelse + ": " + it.ytelseType },
            betingetTjenestepensjonErInkludert = response.utbetalingsListe.any { it.ytelseType == KLPYtelse.BTP.name },
            erSisteOrdning = response.arsakIngenUtbetaling.none { it.statusKode == ANNEN_TP_ORDNING_BURDE_SIMULERE }
        ).apply {
            serviceData = listOf("Request: ${dto?.toString()}", "Response: $response")
        }
    }


}