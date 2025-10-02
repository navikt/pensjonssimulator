package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk

import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerTjenestepensjonFremtidigInntektDto
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.api.acl.v1.SimulerOffentligTjenestepensjonFra2025SpecV1
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.spk.acl.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SPKMapperTest {
    @Test
    fun mapToResponse() {
        val resp = SPKSimulerTjenestepensjonResponse(
            listOf(InkludertOrdning("3010")),
            listOf(
                Utbetaling(LocalDate.of(2025, 2, 1), listOf(Delytelse("BTP", 141), Delytelse("PAASLAG", 268))),
                Utbetaling(LocalDate.of(2030, 2, 1), listOf(Delytelse("OT6370", 779), Delytelse("PAASLAG", 268)))
            ),
            listOf(AarsakIngenUtbetaling("IKKE_STOETTET", "Ikke stoettet", "SAERALDERSPAASLAG"))
        )

        val result = SPKMapper.mapToResponse(resp)

        assertEquals(1, result.ordningsListe.size)
        assertEquals("3010", result.ordningsListe[0].tpNummer)
        assertEquals(4, result.utbetalingsperioder.size)

        assertEquals(LocalDate.of(2025, 2, 1), result.utbetalingsperioder[0].fom)
        assertEquals(141, result.utbetalingsperioder[0].maanedligBelop)
        assertEquals("BTP", result.utbetalingsperioder[0].ytelseType)
        assertEquals(268, result.utbetalingsperioder[1].maanedligBelop)
        assertEquals("PAASLAG", result.utbetalingsperioder[1].ytelseType)

        assertEquals(LocalDate.of(2030, 2, 1), result.utbetalingsperioder[2].fom)
        assertEquals(779, result.utbetalingsperioder[2].maanedligBelop)
        assertEquals("OT6370", result.utbetalingsperioder[2].ytelseType)
        assertEquals(268, result.utbetalingsperioder[3].maanedligBelop)
        assertEquals("PAASLAG", result.utbetalingsperioder[3].ytelseType)

        assertEquals(1, result.aarsakIngenUtbetaling.size)
        assertEquals("Ikke stoettet: SAERALDERSPAASLAG", result.aarsakIngenUtbetaling[0])

    }

    @Test
    fun `map request hvor bruker ber om aa beregne AFP`() {
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
            erApoteker = false
        )

        val result: SPKSimulerTjenestepensjonRequest = SPKMapper.mapToRequest(request)

        assertEquals("12345678901", result.personId)
        assertEquals(5, result.uttaksListe.size)
        assertEquals("PAASLAG", result.uttaksListe[0].ytelseType) //"PAASLAG", "APOF2020", "OT6370", "SAERALDERSPAASLAG"
        assertEquals(uttaksdato, result.uttaksListe[0].fraOgMedDato)
        assertEquals("APOF2020", result.uttaksListe[1].ytelseType)
        assertEquals(uttaksdato, result.uttaksListe[1].fraOgMedDato)
        assertEquals("OT6370", result.uttaksListe[2].ytelseType)
        assertEquals(uttaksdato, result.uttaksListe[2].fraOgMedDato)
        assertEquals("SAERALDERSPAASLAG", result.uttaksListe[3].ytelseType)
        assertEquals(uttaksdato, result.uttaksListe[3].fraOgMedDato)
        assertEquals("OAFP", result.uttaksListe[4].ytelseType)
        assertEquals(uttaksdato, result.uttaksListe[4].fraOgMedDato)
        assertEquals(100000, result.fremtidigInntektListe[0].aarligInntekt)
        assertEquals(LocalDate.parse("${LocalDate.now().year - 1}-01-01"), result.fremtidigInntektListe[0].fraOgMedDato)
        assertEquals(0, result.fremtidigInntektListe[1].aarligInntekt)
        assertEquals(uttaksdato, result.fremtidigInntektListe[1].fraOgMedDato)
        assertEquals(3, result.aarIUtlandetEtter16)
        assertTrue(result.epsPensjon)
        assertTrue(result.eps2G)
    }

    @Test
    fun `map request hvor bruker IKKE ber om aa beregne AFP`() {
        val uttaksdato = LocalDate.of(2025, 2, 1)
        val request = SimulerOffentligTjenestepensjonFra2025SpecV1(
            pid = "12345678901",
            sisteInntekt = 100000,
            aarIUtlandetEtter16 = 3,
            epsPensjon = true,
            eps2G = true,
            brukerBaOmAfp = false,
            uttaksdato = uttaksdato,
            foedselsdato = LocalDate.of(1990, 1, 1),
            erApoteker = false
        )

        val result: SPKSimulerTjenestepensjonRequest = SPKMapper.mapToRequest(request)

        assertEquals("12345678901", result.personId)
        assertEquals(5, result.uttaksListe.size)
        assertEquals("PAASLAG", result.uttaksListe[0].ytelseType) //"PAASLAG", "APOF2020", "OT6370", "SAERALDERSPAASLAG"
        assertEquals(uttaksdato, result.uttaksListe[0].fraOgMedDato)
        assertEquals("APOF2020", result.uttaksListe[1].ytelseType)
        assertEquals(uttaksdato, result.uttaksListe[1].fraOgMedDato)
        assertEquals("OT6370", result.uttaksListe[2].ytelseType)
        assertEquals(uttaksdato, result.uttaksListe[2].fraOgMedDato)
        assertEquals("SAERALDERSPAASLAG", result.uttaksListe[3].ytelseType)
        assertEquals(uttaksdato, result.uttaksListe[3].fraOgMedDato)
        assertEquals("BTP", result.uttaksListe[4].ytelseType)
        assertEquals(uttaksdato, result.uttaksListe[4].fraOgMedDato)
        assertEquals(100000, result.fremtidigInntektListe[0].aarligInntekt)
        assertEquals(LocalDate.parse("${LocalDate.now().year - 1}-01-01"), result.fremtidigInntektListe[0].fraOgMedDato)
        assertEquals(0, result.fremtidigInntektListe[1].aarligInntekt)
        assertEquals(uttaksdato, result.fremtidigInntektListe[1].fraOgMedDato)
        assertEquals(3, result.aarIUtlandetEtter16)
        assertTrue(result.epsPensjon)
        assertTrue(result.eps2G)
    }

    @Test
    fun `map request hvor bruker har ulike fremtidige inntekter`() {
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

        val result: SPKSimulerTjenestepensjonRequest = SPKMapper.mapToRequest(request)

        assertEquals("12345678901", result.personId)
        assertEquals(4, result.fremtidigInntektListe.size)
        assertEquals(request.sisteInntekt, result.fremtidigInntektListe[0].aarligInntekt)
        assertTrue(result.fremtidigInntektListe[0].fraOgMedDato.isBefore(LocalDate.now().minusYears(1)))
        assertEquals(4, result.fremtidigInntektListe[1].aarligInntekt)
        assertEquals(LocalDate.of(2025, 2, 1), result.fremtidigInntektListe[1].fraOgMedDato)
        assertEquals(5, result.fremtidigInntektListe[2].aarligInntekt)
        assertEquals(LocalDate.of(2026, 3, 1), result.fremtidigInntektListe[2].fraOgMedDato)
        assertEquals(6, result.fremtidigInntektListe[3].aarligInntekt)
        assertEquals(LocalDate.of(2027, 4, 1), result.fremtidigInntektListe[3].fraOgMedDato)

    }

    @Test
    fun `map response som er siste ordning`() {
        val resp = SPKSimulerTjenestepensjonResponse(
            listOf(InkludertOrdning("3010")),
            listOf(
                Utbetaling(LocalDate.of(2025, 2, 1), listOf(Delytelse("BTP", 141), Delytelse("PAASLAG", 268))),
                Utbetaling(LocalDate.of(2030, 2, 1), listOf(Delytelse("OT6370", 779), Delytelse("PAASLAG", 268)))
            ),
            listOf(AarsakIngenUtbetaling("IKKE_STOETTET", "Ikke stoettet", "SAERALDERSPAASLAG"))
        )

        val result = SPKMapper.mapToResponse(resp)

        assertTrue(result.erSisteOrdning)
    }

    @Test
    fun `map response som ikke er siste ordning`() {
        val resp = SPKSimulerTjenestepensjonResponse(
            listOf(InkludertOrdning("3010")),
            listOf(
                Utbetaling(LocalDate.of(2025, 2, 1), listOf(Delytelse("BTP", 141), Delytelse("PAASLAG", 268))),
                Utbetaling(LocalDate.of(2030, 2, 1), listOf(Delytelse("OT6370", 779), Delytelse("PAASLAG", 268)))
            ),
            listOf(AarsakIngenUtbetaling("IKKE_SISTE_ORDNING", "Ikke siste ordning", ""))
        )

        val result = SPKMapper.mapToResponse(resp)

        assertFalse(result.erSisteOrdning)

    }
}