package no.nav.pensjon.simulator.afp.offentlig.fra2025.grunnlag

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpOffentligLivsvarigGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.spec.InnvilgetLivsvarigOffentligAfpSpec
import no.nav.pensjon.simulator.g.GrunnbeloepService
import java.time.LocalDate

class LivsvarigOffentligAfpGrunnlagServiceTest : ShouldSpec({

    should("bruke nåværende grunnbeløp når 'sist regulert G' er udefinert") {
        LivsvarigOffentligAfpGrunnlagService(grunnbeloepService = arrangeGrunnbeloep()).livsvarigOffentligAfpGrunnlag(
            innvilgetAfpSpec = null,
            simulertAfpYtelseListe = listOf(
                ytelse(afpYtelsePerAar = 12.3, gjelderFom = LocalDate.of(2025, 1, 1)), // before
                ytelse(afpYtelsePerAar = 23.4, gjelderFom = LocalDate.of(2025, 2, 1)), // on => max gjelderFom
                ytelse(afpYtelsePerAar = 34.5, gjelderFom = LocalDate.of(2025, 3, 1))  // after
            ),
            kravhode = Kravhode(),
            maxGjelderFom = LocalDate.of(2025, 2, 1)
        ) shouldBe AfpOffentligLivsvarigGrunnlag(
            sistRegulertG = 123000, // from GrunnbeloepService
            bruttoPerAr = 23.4, // from selected LivsvarigOffentligAfpYtelseMedDelingstall
            uttaksdato = LocalDate.of(2025, 2, 1), // ditto
            virkTom = null // not assigned
        )
    }

    /**
     * Når ingen ytelser i AFP-beregningsresultatet skal grunnlaget baseres på saksbehandlet AFP (fra persongrunnlaget).
     */
    should("bruke saksbehandlet AFP når både spesifisert innvilget AFP og simulert AFP mangler") {
        LivsvarigOffentligAfpGrunnlagService(grunnbeloepService = arrangeGrunnbeloep()).livsvarigOffentligAfpGrunnlag(
            innvilgetAfpSpec = null,
            simulertAfpYtelseListe = emptyList(), // ingen ytelser => use gjeldendeInnvilgetLivsvarigOffentligAfpGrunnlag
            kravhode = Kravhode().apply {
                onsketVirkningsdato = LocalDate.of(2025, 2, 1)
                persongrunnlagListe = mutableListOf(persongrunnlag())
            },
            maxGjelderFom = LocalDate.of(2025, 2, 1)
        ) shouldBe AfpOffentligLivsvarigGrunnlag(
            sistRegulertG = 122000, // from selected AfpOffentligLivsvarigGrunnlag in Persongrunnlag
            bruttoPerAr = 2000.2, // ditto
            uttaksdato = LocalDate.of(2025, 1, 1), // ditto
            virkTom = LocalDate.of(2025, 12, 31) // ditto
        )
    }

    /**
     * Når simuleringsspesifikasjonen inneholder innvilget AFP, så skal denne brukes framfor både simulert AFP og saksbehandlet AFP.
     */
    should("foretrekke å bruke spesifisert innvilget AFP") {
        LivsvarigOffentligAfpGrunnlagService(grunnbeloepService = arrangeGrunnbeloep()).livsvarigOffentligAfpGrunnlag(
            // Denne innvilgede ytelsen skal ignoreres:
            innvilgetAfpSpec = InnvilgetLivsvarigOffentligAfpSpec(
                aarligBruttoBeloep = 1234.5,
                uttakFom = LocalDate.of(2026, 1, 1),
                sistRegulertGrunnbeloep = 130000
            ),
            simulertAfpYtelseListe = listOf(
                // Denne simulerte ytelsen skal ignoreres:
                ytelse(afpYtelsePerAar = 999.9, gjelderFom = LocalDate.of(2025, 2, 1)),
            ),
            kravhode = Kravhode().apply {
                onsketVirkningsdato = LocalDate.of(2025, 6, 1)
                persongrunnlagListe = mutableListOf(persongrunnlag()) // saksbehandlet AFP skal ignoreres
            },
            maxGjelderFom = LocalDate.of(2025, 2, 1)
        ) shouldBe AfpOffentligLivsvarigGrunnlag(
            sistRegulertG = 130000,
            bruttoPerAr = 1234.5,
            uttaksdato = LocalDate.of(2026, 1, 1),
            virkTom = null
        )
    }
})

private fun arrangeGrunnbeloep() =
    mockk<GrunnbeloepService> {
        every { naavaerendeGrunnbeloep() } returns 123000
    }

private fun ytelse(afpYtelsePerAar: Double, gjelderFom: LocalDate) =
    LivsvarigOffentligAfpYtelseMedDelingstall(
        pensjonBeholdning = 0, // don't care
        afpYtelsePerAar, // mappes til bruttoPerAr
        delingstall = 0.0, // don't care
        gjelderFom, // mappes til uttaksdato
        gjelderFomAlder = Alder(63, 0) // don't care
    )

private fun persongrunnlag() =
    Persongrunnlag().apply {
        livsvarigOffentligAfpGrunnlagListe = listOf(
            AfpOffentligLivsvarigGrunnlag(
                sistRegulertG = 111000,
                bruttoPerAr = 1000.1,
                uttaksdato = LocalDate.of(2024, 1, 1), // outside...
                virkTom = LocalDate.of(2024, 12, 31) // ...onsketVirkningsdato => ignored
            ),
            AfpOffentligLivsvarigGrunnlag(
                sistRegulertG = 122000,
                bruttoPerAr = 2000.2,
                uttaksdato = LocalDate.of(2025, 1, 1), // within...
                virkTom = LocalDate.of(2025, 12, 31) // ...onsketVirkningsdato => selected
            )
        )
        personDetaljListe = mutableListOf(
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
            }
        )
    }
