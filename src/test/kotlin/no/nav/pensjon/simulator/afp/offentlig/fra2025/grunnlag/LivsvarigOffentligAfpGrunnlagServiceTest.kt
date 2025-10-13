package no.nav.pensjon.simulator.afp.offentlig.fra2025.grunnlag

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpOffentligLivsvarigGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.g.GrunnbeloepService
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class LivsvarigOffentligAfpGrunnlagServiceTest : FunSpec({

    test("livsvarigOffentligAfpGrunnlag should fetch grunnbeløp when sistRegulertG undefined") {
        LivsvarigOffentligAfpGrunnlagService(grunnbeloepService = arrangeGrunnbeloep()).livsvarigOffentligAfpGrunnlag(
            afpResult = LivsvarigOffentligAfpResult(
                pid = pid.value,
                afpYtelseListe = listOf(
                    ytelse(afpYtelsePerAar = 12.3, gjelderFom = LocalDate.of(2025, 1, 1)), // before
                    ytelse(afpYtelsePerAar = 23.4, gjelderFom = LocalDate.of(2025, 2, 1)), // on => max gjelderFom
                    ytelse(afpYtelsePerAar = 34.5, gjelderFom = LocalDate.of(2025, 3, 1))  // after
                )
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
     * Når ingen ytelser i AFP-beregningsresultatet skal grunnlaget baseres på innvilget AFP (fra persongrunnlaget).
     */
    test("livsvarigOffentligAfpGrunnlag should use innvilget AFP when no ytelse in AFP-result") {
        LivsvarigOffentligAfpGrunnlagService(grunnbeloepService = arrangeGrunnbeloep()).livsvarigOffentligAfpGrunnlag(
            afpResult = LivsvarigOffentligAfpResult(
                pid = pid.value,
                afpYtelseListe = emptyList() // ingen ytelser => use gjeldendeInnvilgetLivsvarigOffentligAfpGrunnlag
            ),
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
