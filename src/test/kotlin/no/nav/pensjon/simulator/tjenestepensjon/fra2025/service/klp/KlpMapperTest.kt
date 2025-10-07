package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp

import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerTjenestepensjonFremtidigInntektDto
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerOffentligTjenestepensjonFra2025SpecV1
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.SimulertTjenestepensjon
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.KlpMapper.ANNEN_TP_ORDNING_BURDE_SIMULERE
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class KlpMapperTest{

    @Test
    fun `map klp response`() {
        val resp = KlpSimulerTjenestepensjonResponse(
            listOf(InkludertOrdning("1000")),
            listOf(
                Utbetaling(LocalDate.of(2025, 2, 6), manedligUtbetaling = 1, arligUtbetaling = 12, ytelseType = "BTP"),
                Utbetaling(
                    LocalDate.of(2028, 3, 7),
                    manedligUtbetaling = 2,
                    arligUtbetaling = 24,
                    ytelseType = "PAASLAG"
                ),
                Utbetaling(
                    LocalDate.of(2030, 4, 8),
                    manedligUtbetaling = 3,
                    arligUtbetaling = 36,
                    ytelseType = "OT6370"
                ),
            ),
            listOf(
                ArsakIngenUtbetaling(
                    statusKode = "IKKE_STOETTET",
                    statusBeskrivelse = "Ikke stoettet",
                    ytelseType = "SAERALDERSPAASLAG"
                )
            ), true
        )

        val result = KlpMapper.mapToResponse(resp)

        assertEquals(1, result.ordningsListe.size)
        assertEquals("1000", result.ordningsListe[0].tpNummer)
        assertEquals(3, result.utbetalingsperioder.size)

        assertEquals(resp.utbetalingsListe[0].fraOgMedDato, result.utbetalingsperioder[0].fom)
        assertEquals(resp.utbetalingsListe[0].manedligUtbetaling, result.utbetalingsperioder[0].maanedligBelop)
        assertEquals(resp.utbetalingsListe[0].ytelseType, result.utbetalingsperioder[0].ytelseType)
        assertEquals(resp.utbetalingsListe[1].fraOgMedDato, result.utbetalingsperioder[1].fom)
        assertEquals(resp.utbetalingsListe[1].manedligUtbetaling, result.utbetalingsperioder[1].maanedligBelop)
        assertEquals(resp.utbetalingsListe[1].ytelseType, result.utbetalingsperioder[1].ytelseType)
        assertEquals(resp.utbetalingsListe[2].fraOgMedDato, result.utbetalingsperioder[2].fom)
        assertEquals(resp.utbetalingsListe[2].manedligUtbetaling, result.utbetalingsperioder[2].maanedligBelop)
        assertEquals(resp.utbetalingsListe[2].ytelseType, result.utbetalingsperioder[2].ytelseType)

        assertEquals(1, result.aarsakIngenUtbetaling.size)
        assertEquals("Ikke stoettet: SAERALDERSPAASLAG", result.aarsakIngenUtbetaling.first())
        assertTrue { result.erSisteOrdning }

    }

    @Test
    fun `map klp response med ikke siste ordning`() {
        val statusBeskrivelse = "Ikke siste ordning. Statens pensjonskasse er siste ordning"
        val ytelseType = "ALLE"
        val resp = KlpSimulerTjenestepensjonResponse(
            listOf(InkludertOrdning("1000")),
            listOf(),
            listOf(ArsakIngenUtbetaling(statusKode = ANNEN_TP_ORDNING_BURDE_SIMULERE, statusBeskrivelse = statusBeskrivelse, ytelseType = ytelseType)), false
        )

        val result: SimulertTjenestepensjon = KlpMapper.mapToResponse(resp)

        assertEquals(1, result.ordningsListe.size)
        assertEquals("1000", result.ordningsListe[0].tpNummer)
        assertTrue(result.utbetalingsperioder.isEmpty())

        assertEquals(1, result.aarsakIngenUtbetaling.size)
        assertEquals("$statusBeskrivelse: $ytelseType", result.aarsakIngenUtbetaling.first())
        assertFalse { result.erSisteOrdning }

    }

    @Test
    fun `map request to klp request with fremtidigeInntekter`() {
        val uttaksdato = LocalDate.of(2025, 2, 1)
        val request = SimulerOffentligTjenestepensjonFra2025SpecV1(
            pid = "12345678901",
            sisteInntekt = 100000,
            aarIUtlandetEtter16 = 3,
            epsPensjon = true,
            eps2G = true,
            brukerBaOmAfp = true,
            uttaksdato = uttaksdato,
            foedselsdato = LocalDate.of(1990, 1, 1),
            fremtidigeInntekter = listOf(
                SimulerTjenestepensjonFremtidigInntektDto(LocalDate.of(2025, 2, 1), 4),
                SimulerTjenestepensjonFremtidigInntektDto(LocalDate.of(2026, 3, 1), 5),
                SimulerTjenestepensjonFremtidigInntektDto(LocalDate.of(2027, 4, 1), 6)
            ),
            erApoteker = false
        )

        val result: KlpSimulerTjenestepensjonRequest = KlpMapper.mapToRequest(request)

        assertEquals(request.pid, result.personId)
        assertEquals(1, result.uttaksListe.size)
        assertEquals("ALLE", result.uttaksListe[0].ytelseType)
        assertEquals(uttaksdato, result.uttaksListe[0].fraOgMedDato)

        assertEquals(4, result.fremtidigInntektsListe.size)

        assertEquals(request.sisteInntekt, result.fremtidigInntektsListe[0].arligInntekt)
        assertTrue(result.fremtidigInntektsListe[0].fraOgMedDato.isEqual(LocalDate.now()) ||
                result.fremtidigInntektsListe[0].fraOgMedDato.isBefore(LocalDate.now().plusDays(1)))


        assertEquals(request.fremtidigeInntekter!![0].aarligInntekt, result.fremtidigInntektsListe[1].arligInntekt)
        assertEquals(request.fremtidigeInntekter[0].fraOgMed, result.fremtidigInntektsListe[1].fraOgMedDato)
        assertEquals(request.fremtidigeInntekter[1].aarligInntekt, result.fremtidigInntektsListe[2].arligInntekt)
        assertEquals(request.fremtidigeInntekter[1].fraOgMed, result.fremtidigInntektsListe[2].fraOgMedDato)
        assertEquals(request.fremtidigeInntekter[2].aarligInntekt, result.fremtidigInntektsListe[3].arligInntekt)
        assertEquals(request.fremtidigeInntekter[2].fraOgMed, result.fremtidigInntektsListe[3].fraOgMedDato)

        assertEquals(request.aarIUtlandetEtter16, result.arIUtlandetEtter16)
        assertTrue(result.epsPensjon)
        assertTrue(result.eps2G)
    }

    @Test
    fun `map request to klp request without fremtidigeInntekter`() {
        val uttaksdato = LocalDate.of(2025, 2, 1)
        val request = SimulerOffentligTjenestepensjonFra2025SpecV1(
            pid = "12345678901",
            sisteInntekt = 100000,
            aarIUtlandetEtter16 = 3,
            epsPensjon = true,
            eps2G = true,
            brukerBaOmAfp = true,
            uttaksdato = uttaksdato,
            foedselsdato = LocalDate.of(1990, 1, 1),
            fremtidigeInntekter = null,
            erApoteker = false
        )

        val result: KlpSimulerTjenestepensjonRequest = KlpMapper.mapToRequest(request)

        assertEquals(request.pid, result.personId)
        assertEquals(1, result.uttaksListe.size)
        assertEquals("ALLE", result.uttaksListe[0].ytelseType)
        assertEquals(uttaksdato, result.uttaksListe[0].fraOgMedDato)

        assertEquals(1, result.fremtidigInntektsListe.size)

        assertEquals(request.sisteInntekt, result.fremtidigInntektsListe[0].arligInntekt)
        assertTrue(result.fremtidigInntektsListe[0].fraOgMedDato.isEqual(LocalDate.now()) ||
                result.fremtidigInntektsListe[0].fraOgMedDato.isBefore(LocalDate.now().plusDays(1)))

        assertEquals(request.aarIUtlandetEtter16, result.arIUtlandetEtter16)
        assertTrue(result.epsPensjon)
        assertTrue(result.eps2G)
    }
}