package no.nav.pensjon.simulator.core.result

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.beregn.BeholdningPeriode
import java.time.LocalDate

class SimulertAlderspensjonTest : FunSpec({

    /**
     * Forutsetninger:
     * - flere garantipensjonsbeholdninger med f.o.m.-dato før angitt dato
     * - en garantipensjonsbeholdninger med f.o.m.-dato veldig nært, men etter, angitt dato
     * Da skal funksjonen 'garantipensjonsbeholdningVedDato':
     * - velge garantipensjonsbeholdningen med f.o.m.-dato nærmest før angitt dato
     * - returnere heltallsverdien (avrundet nedover) av valgt garantipensjonsbeholdning
     */
    test("garantipensjonsbeholdningVedDato should return integer av garantipensjonsbeholdning med f.o.m.-dato nærmest før angitt dato") {
        SimulertAlderspensjon().apply {
            pensjonBeholdningListe = listOf(
                // veldig nært, men etter, angitt dato:
                beholdningPeriode(fom = LocalDate.of(2021, 7, 1), garantipensjonsbeholdning = 7.9),
                // før og ganske nært angitt dato (denne skal velges):
                beholdningPeriode(fom = LocalDate.of(2021, 4, 1), garantipensjonsbeholdning = 4.9),
                // før, men ikke nært, angitt dato:
                beholdningPeriode(fom = LocalDate.of(2020, 1, 1), garantipensjonsbeholdning = 1.9)
            )
        }.garantipensjonsbeholdningVedDato(dato = LocalDate.of(2021, 6, 1)) shouldBe 4 // avrundet nedover
    }

    /**
     * Forutsetninger:
     * - ingen garantipensjonsbeholdning med f.o.m.-dato før angitt dato
     * - flere garantipensjonsbeholdninger med f.o.m.-dato etter angitt dato
     * Da skal funksjonen 'garantipensjonsbeholdningVedDato':
     * - velge garantipensjonsbeholdningen med f.o.m.-dato nærmest angitt dato
     * - returnere heltallsverdien av valgt garantipensjonsbeholdning
     */
    test("garantipensjonsbeholdningVedDato hvis ingen med f.o.m.-dato før angitt dato") {
        SimulertAlderspensjon().apply {
            pensjonBeholdningListe = listOf(
                // etter og veldig nært angitt dato:
                beholdningPeriode(fom = LocalDate.of(2021, 7, 1), garantipensjonsbeholdning = 7.8),
                // etter og ikke nært angitt dato:
                beholdningPeriode(fom = LocalDate.of(2025, 1, 1), garantipensjonsbeholdning = 1.8)
            )
        }.garantipensjonsbeholdningVedDato(dato = LocalDate.of(2021, 6, 1)) shouldBe 7 // avrundet nedover
    }

    test("Hvis ingen beholdninger, garantipensjonsbeholdningVedDato should return null") {
        SimulertAlderspensjon().apply {
            pensjonBeholdningListe = listOf(
                beholdningPeriode(fom = LocalDate.of(2021, 7, 1), garantipensjonsbeholdning = null)
            )
        }.garantipensjonsbeholdningVedDato(dato = LocalDate.of(2021, 6, 1)) shouldBe null

        SimulertAlderspensjon().apply {
            pensjonBeholdningListe = emptyList()
        }.garantipensjonsbeholdningVedDato(dato = LocalDate.of(2021, 6, 1)) shouldBe null
    }
})

private fun beholdningPeriode(fom: LocalDate, garantipensjonsbeholdning: Double?) =
    BeholdningPeriode(
        datoFom = fom,
        pensjonsbeholdning = null,
        garantipensjonsbeholdning,
        garantitilleggsbeholdning = null,
        garantipensjonsniva = null
    )
